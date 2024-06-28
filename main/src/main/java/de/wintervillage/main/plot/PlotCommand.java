package de.wintervillage.main.plot;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.util.BoundingBox2D;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.List;

public class PlotCommand {

    /**
     * /gs setup | Starts the setup and gives the player an axe to select the corners
     * /gs info (uniqueId) | Gives information about the plot the player is standing on
     * /gs list | Lists all plots
     * /gs create <name> | Creates a plot with the name
     * /gs delete (name) | Deletes the plot with the name
     * /gs tp <uniqueId> | Teleports to the plot with the uniqueId
     * /gs <name> owner <player> | Sets the owner of the plot
     * /gs <name> member add <player> | Adds a player to the plot
     * /gs <name> member remove <player> | Removes a player from the plot
     */

    private final WinterVillage winterVillage;

    public PlotCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        // TODO: LuckPerms
        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("gs")
                .requires((source) -> source.getSender() instanceof Player)
                .then(
                        Commands.literal("create")
                                .then(
                                        Commands.argument("name", StringArgumentType.word())
                                                .executes((source) -> {
                                                    String name = StringArgumentType.getString(source, "name");
                                                    final Player player = (Player) source.getSource().getSender();

                                                    // TODO: Check max amount of plots by player

                                                    Plot plot = new Plot(
                                                            name,
                                                            this.winterVillage.plotHandler.generateId(6),
                                                            new Date(),
                                                            player.getUniqueId(),
                                                            new BoundingBox2D(),
                                                            List.of()
                                                    );

                                                    this.winterVillage.plotDatabase.insertAsync(plot)
                                                            .thenAccept((v) -> {
                                                                player.sendMessage(Component.text("Plot inserted", NamedTextColor.GREEN));
                                                                this.winterVillage.plotHandler.plotCache.add(plot);
                                                            })
                                                            .exceptionally((t) -> {
                                                                player.sendMessage(Component.text("Could not insert plot", NamedTextColor.RED));
                                                                return null;
                                                            });

                                                    return 1;
                                                })
                                )
                )
                .then(
                        Commands.literal("delete")
                                .executes((source) -> {
                                    final Player player = (Player) source.getSource().getSender();

                                    Plot plot = this.winterVillage.plotHandler.byBounds(player.getLocation());
                                    if (plot == null) {
                                        player.sendMessage(Component.text("You are not standing in a plot", NamedTextColor.RED));
                                        return 0;
                                    }

                                    // TODO: LuckPerms ( && !player.hasPermission("...") )
                                    if (!plot.getOwner().equals(player.getUniqueId())) {
                                        player.sendMessage(Component.text("You are not the owner of this plot", NamedTextColor.RED));
                                        return 0;
                                    }

                                    this.winterVillage.plotDatabase.deleteAsync(plot.getUniqueId())
                                            .thenAccept((v) -> {
                                                player.sendMessage(Component.text("Deleted plot", NamedTextColor.GREEN));
                                                this.winterVillage.plotHandler.plotCache.remove(plot);
                                            })
                                            .exceptionally((t) -> {
                                                player.sendMessage(Component.text("Could not delete plot", NamedTextColor.RED));
                                                return null;
                                            });

                                    return 1;
                                })
                                .then(
                                        Commands.argument("uniqueId", StringArgumentType.word())
                                                // TODO: LuckPerms
                                                .executes((source) -> {
                                                    String uniqueId = StringArgumentType.getString(source, "uniqueId");
                                                    final Player player = (Player) source.getSource().getSender();

                                                    Plot plot = this.winterVillage.plotHandler.getPlotById(uniqueId);
                                                    if (plot == null) {
                                                        player.sendMessage(Component.text("Could not find plot", NamedTextColor.RED));
                                                        return 0;
                                                    }

                                                    this.winterVillage.plotDatabase.deleteAsync(plot.getUniqueId())
                                                            .thenAccept((v) -> {
                                                                player.sendMessage(Component.text("Deleted plot", NamedTextColor.GREEN));
                                                                this.winterVillage.plotHandler.plotCache.remove(plot);
                                                            })
                                                            .exceptionally((t) -> {
                                                                player.sendMessage(Component.text("Could not delete plot", NamedTextColor.RED));
                                                                return null;
                                                            });
                                                    return 1;
                                                })
                                )
                )
                .then(
                        Commands.literal("test")
                                .then(
                                        Commands.argument("uniqueId", StringArgumentType.word())
                                                .executes((source) -> {
                                                            String uniqueId = StringArgumentType.getString(source, "uniqueId");
                                                            final Player player = (Player) source.getSource().getSender();

                                                            Plot plot = this.winterVillage.plotHandler.getPlotById(uniqueId);
                                                            if (plot == null) {
                                                                player.sendMessage(Component.text("Could not find plot", NamedTextColor.RED));
                                                                return 0;
                                                            }

                                                            ParticleRectangle rectangle = new ParticleRectangle(player);
                                                            rectangle.setBoundingBox(plot.getBoundingBox());
                                                            rectangle.start();
                                                            return 1;
                                                        }
                                                )
                                )
                );
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Manage your plots", List.of());
    }
}
