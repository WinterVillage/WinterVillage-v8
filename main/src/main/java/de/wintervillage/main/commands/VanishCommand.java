package de.wintervillage.main.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class VanishCommand {

    private final WinterVillage winterVillage;

    public VanishCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        final LiteralArgumentBuilder builder = Commands.literal("vanish")
                .requires((source) -> source.getSender() instanceof Player)
                .requires((source) -> source.getSender().hasPermission("wintervillage.command.vanish"))
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    this.winterVillage.playerDatabase.modify(player.getUniqueId(), winterVillagePlayer -> winterVillagePlayer.vanished(!winterVillagePlayer.vanished()))
                            .thenAccept(winterVillagePlayer -> {
                                if (winterVillagePlayer.vanished()) {
                                    player.sendMessage(Component.join(
                                            this.winterVillage.prefix,
                                            Component.translatable("wintervillage.command.vanish.hidden")
                                    ));
                                    Bukkit.getScheduler().runTask(this.winterVillage, () -> this.winterVillage.playerHandler.hidePlayer(player));
                                    return;
                                }

                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.command.vanish.shown")
                                ));
                                Bukkit.getScheduler().runTask(this.winterVillage, () -> this.winterVillage.playerHandler.showPlayer(player));
                                return;
                            });
                    return Command.SINGLE_SUCCESS;
                });
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Teleport you to the spawn", List.of());
    }
}
