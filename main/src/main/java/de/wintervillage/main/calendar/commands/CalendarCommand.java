package de.wintervillage.main.calendar.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.calendar.CalendarDay;
import de.wintervillage.main.calendar.CalendarInventory;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;

public class CalendarCommand {

    private WinterVillage winterVillage;

    public CalendarCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("adventskalender")
                .requires(source -> source.getExecutor() instanceof Player)
                .then(Commands.literal("set")
                        // TODO: LuckPerms
                        .then(Commands.argument("day", IntegerArgumentType.integer(1, 24))
                                .executes((source) -> {
                                    Player player = (Player) source.getSource().getExecutor();
                                    int day = IntegerArgumentType.getInteger(source, "day");

                                    CalendarDay calendarDay = new CalendarDay(
                                            day,
                                            player.getInventory().getItemInMainHand(),
                                            List.of()
                                    );

                                    this.winterVillage.calendarDatabase.insertAsync(calendarDay)
                                            .thenAccept((v) -> {
                                                player.sendMessage(Component.text("Inserted"));
                                                this.winterVillage.calendarHandler.forceUpdate();
                                            })
                                            .exceptionally((t) -> {
                                                player.sendMessage(Component.text("Failed to insert"));
                                                return null;
                                            });

                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("get")
                        // TODO: LuckPerms
                        .then(Commands.argument("day", IntegerArgumentType.integer(1, 24))
                                .executes((source) -> {
                                    Player player = (Player) source.getSource().getExecutor();
                                    int day = IntegerArgumentType.getInteger(source, "day");

                                    Optional<CalendarDay> optional = this.winterVillage.calendarHandler.byDay(day);
                                    if (!optional.isPresent()) {
                                        player.sendMessage(Component.text("Not found"));
                                        return 0;
                                    }

                                    player.getInventory().addItem(optional.get().getItemStack());
                                    return 1;
                                })
                        )
                )
                .executes((source) -> {
                    Player player = (Player) source.getSource().getExecutor();

                    /**
                    if (!this.winterVillage.calendarHandler.withinRange()) {
                        player.sendMessage(Component.text("Not available"));
                        return 0;
                    }
                     */

                    CalendarInventory inventory = new CalendarInventory(player);
                    player.openInventory(inventory.getInventory());
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1.6f);

                    return 1;
                });

        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Öffnet den Adventskalender.", List.of("ak"));
    }
}
