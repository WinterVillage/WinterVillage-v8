package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class TpSubCommand {

    private final WinterVillage winterVillage;

    public TpSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("tp")
                .requires((source) -> source.getSender().hasPermission("wintervillage.plot.command.teleport"))
                .then(Commands.argument("uniqueId", ArgumentTypes.uuid())
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            final UUID uniqueId = source.getArgument("uniqueId", UUID.class);
                            Plot plot = this.winterVillage.plotHandler.byUniqueId(uniqueId);
                            if (plot == null) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.not-found-by-uniqueId", Component.text(uniqueId.toString()))
                                ));
                                return 0;
                            }

                            int highestBlockAt = Bukkit.getWorld("world").getHighestBlockYAt(
                                    (int) plot.boundingBox().getCenterX(),
                                    (int) plot.boundingBox().getCenterZ(),
                                    HeightMap.WORLD_SURFACE
                            );
                            Location location = new Location(
                                    Bukkit.getWorld("world"),
                                    plot.boundingBox().getCenterX(),
                                    highestBlockAt + 1,
                                    plot.boundingBox().getCenterZ()
                            );

                            player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN)
                                    .thenAccept(_ -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.teleported")
                                        ));
                                    })
                                    .exceptionally(throwable -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.failed-to-teleport", throwable.getMessage())
                                        ));
                                        return null;
                                    });
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
