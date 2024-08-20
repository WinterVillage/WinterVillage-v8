package de.wintervillage.main.plot.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.common.paper.persistent.BoundingBoxDataType;
import de.wintervillage.main.plot.ParticleRectangle;
import de.wintervillage.main.plot.Plot;
import de.wintervillage.main.plot.impl.PlotImpl;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PlotCommand {

    /**
     * /gs setup | Starts the setup and gives the player an axe to select the corners
     * /gs info | Gives information about the plot the player is standing on
     * /gs showBorders | Shows the border of every plot
     * /gs listAll | Lists all plots
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

        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("gs")
                .requires((source) -> source.getSender() instanceof Player)
                .then(Commands.literal("setup")
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            if (player.getPersistentDataContainer().has(this.winterVillage.plotHandler.plotSetupKey)
                                    || player.getPersistentDataContainer().has(this.winterVillage.plotHandler.plotRectangleKey)) {
                                player.sendMessage(Component.text("You are already setting up a plot", NamedTextColor.RED));
                                return 0;
                            }

                            boolean hasPlot = !this.winterVillage.plotHandler.byOwner(player.getUniqueId()).isEmpty();
                            boolean canBypass = player.hasPermission("wintervillage.plot.ignore_limit");
                            if (hasPlot && !canBypass) {
                                player.sendMessage(Component.text("You already have a plot", NamedTextColor.RED));
                                return 0;
                            }

                            ParticleRectangle rectangle = new ParticleRectangle(player);
                            int taskId = rectangle.start();

                            player.getPersistentDataContainer().set(this.winterVillage.plotHandler.plotRectangleKey, PersistentDataType.INTEGER, taskId);
                            player.getPersistentDataContainer().set(this.winterVillage.plotHandler.plotSetupKey, new BoundingBoxDataType(), new BoundingBox2D());
                            player.getInventory().addItem(this.winterVillage.plotHandler.SETUP_ITEM);
                            return 1;
                        })
                )
                .then(Commands.literal("info")
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            Plot plot = this.winterVillage.plotHandler.byBounds(player.getLocation());
                            if (plot == null) {
                                player.sendMessage(Component.text("You are not standing in a plot", NamedTextColor.RED));
                                return 0;
                            }

                            // TODO: message
                            return 1;
                        })
                )
                .then(Commands.literal("listAll")
                        .requires((source) -> source.getSender().hasPermission("wintervillage.plot.command.listAll"))
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            if (this.winterVillage.plotHandler.getPlotCache().isEmpty()) {
                                player.sendMessage(Component.text("No plots found", NamedTextColor.RED));
                                return 1;
                            }

                            // TODO: message

                            return 1;
                        }))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes((source) -> {
                                    String name = StringArgumentType.getString(source, "name");
                                    final Player player = (Player) source.getSource().getSender();

                                    PersistentDataContainer container = player.getPersistentDataContainer();

                                    if (!container.has(this.winterVillage.plotHandler.plotSetupKey)
                                            || !container.has(this.winterVillage.plotHandler.plotRectangleKey)) {
                                        player.sendMessage(Component.text("You have not set up a plot", NamedTextColor.RED));
                                        return 0;
                                    }

                                    BoundingBox2D boundingBox = container.get(this.winterVillage.plotHandler.plotSetupKey, new BoundingBoxDataType());
                                    boolean tooLarge = (!player.hasPermission("wintervillage.plot.width_bypass")
                                            && (boundingBox.getWidthX() > this.winterVillage.plotHandler.MAX_PLOT_WIDTH
                                            || boundingBox.getWidthZ() > this.winterVillage.plotHandler.MAX_PLOT_WIDTH));
                                    if (tooLarge) {
                                        player.sendMessage(Component.text("Plot is too large", NamedTextColor.RED));
                                        return 0;
                                    }

                                    Plot plot = new PlotImpl(
                                            UUID.randomUUID(),
                                            name,
                                            new Date(),
                                            player.getUniqueId(),
                                            boundingBox,
                                            List.of()
                                    );

                                    this.winterVillage.plotDatabase.insert(plot)
                                            .thenAccept((v) -> {
                                                player.sendMessage(Component.text("Plot inserted", NamedTextColor.GREEN));
                                                this.winterVillage.plotHandler.plotCache.add(plot);
                                            })
                                            .exceptionally((t) -> {
                                                player.sendMessage(Component.text("Could not insert plot", NamedTextColor.RED));
                                                return null;
                                            });

                                    this.winterVillage.plotHandler.stopSetup(player);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("delete")
                        .then(Commands.argument("uniqueId", ArgumentTypes.uuid())
                                .requires((source) -> source.getSender().hasPermission("wintervillage.plot.command.force_delete"))
                                .executes((source) -> {
                                    UUID uniqueId = ArgumentTypes.uuid().parse(new StringReader(source.getArgument("uniqueId", String.class)));
                                    final Player player = (Player) source.getSource().getSender();

                                    Plot plot = this.winterVillage.plotHandler.byUniqueId(uniqueId);
                                    if (plot == null) {
                                        player.sendMessage(Component.text("Plot not found", NamedTextColor.RED));
                                        return 0;
                                    }

                                    this.winterVillage.plotDatabase.delete(plot.uniqueId())
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
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            Plot plot = this.winterVillage.plotHandler.byBounds(player.getLocation());
                            if (plot == null) {
                                player.sendMessage(Component.text("You are not standing in a plot", NamedTextColor.RED));
                                return 0;
                            }

                            boolean notOwner = !plot.owner().equals(player.getUniqueId());
                            boolean canBypass = player.hasPermission("wintervillage.plot.ignore_owner");
                            if (notOwner && !canBypass) {
                                player.sendMessage(Component.text("You are not the owner of this plot", NamedTextColor.RED));
                                return 0;
                            }

                            this.winterVillage.plotDatabase.delete(plot.uniqueId())
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
                );
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Manage your plots", List.of());
    }
}
