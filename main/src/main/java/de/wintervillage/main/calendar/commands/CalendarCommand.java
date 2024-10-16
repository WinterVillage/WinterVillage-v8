package de.wintervillage.main.calendar.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.calendar.CalendarDay;
import de.wintervillage.main.calendar.CalendarInventory;
import de.wintervillage.main.calendar.impl.CalendarDayImpl;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CalendarCommand {

    private final WinterVillage winterVillage;

    public CalendarCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("adventskalender")
                .requires(source -> source.getExecutor() instanceof Player)
                .then(Commands.literal("set")
                        .requires(source -> source.getSender().hasPermission("wintervillage.calendar.command.set"))
                        .then(Commands.argument("day", IntegerArgumentType.integer(1, 24))
                                .executes((source) -> {
                                    Player player = (Player) source.getSource().getSender();
                                    int day = IntegerArgumentType.getInteger(source, "day");

                                    CalendarDay calendarDay = new CalendarDayImpl(
                                            day,
                                            player.getInventory().getItemInMainHand(),
                                            List.of()
                                    );

                                    this.winterVillage.calendarDatabase.insert(calendarDay)
                                            .thenAccept(_ -> {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.command.calendar.set-day", Component.text(day))
                                                ));
                                                this.winterVillage.calendarHandler.forceUpdate();
                                            })
                                            .exceptionally((t) -> {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.command.calendar.set-day-error", Component.text(t.getMessage()))
                                                ));
                                                return null;
                                            });

                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("get")
                        .requires(source -> source.getSender().hasPermission("wintervillage.calendar.command.get"))
                        .then(Commands.argument("day", IntegerArgumentType.integer(1, 24))
                                .executes((source) -> {
                                    Player player = (Player) source.getSource().getSender();
                                    int day = IntegerArgumentType.getInteger(source, "day");

                                    Optional<CalendarDay> optional = this.winterVillage.calendarHandler.byDay(day);
                                    if (!optional.isPresent()) {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.command.calendar.get-not-found")
                                        ));
                                        return 0;
                                    }

                                    player.getInventory().addItem(optional.get().itemStack());
                                    return 1;
                                })
                        )
                )
                .executes((source) -> {
                    Player player = (Player) source.getSource().getSender();

                    if (!this.winterVillage.calendarHandler.withinRange()) {
                        String formatted = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(this.winterVillage.calendarHandler.startDate);

                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.calendar.not-available", Component.text(formatted))
                        ));
                        return 0;
                    }

                    CalendarInventory inventory = new CalendarInventory(player);
                    inventory.getGui().open(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1.6f);

                    return 1;
                });

        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Öffnet den Adventskalender.", List.of("ak"));
    }
}
