package de.wintervillage.main.plot.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.common.paper.persistent.BoundingBoxDataType;
import de.wintervillage.main.plot.combined.CombinedUserPlot;
import de.wintervillage.main.plot.task.BoundariesTask;
import de.wintervillage.main.plot.task.SetupTask;
import de.wintervillage.main.plot.Plot;
import de.wintervillage.main.plot.impl.PlotImpl;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlotCommand {

    /**
     * /gs setup | Starts the setup and gives the player an axe to select the corners
     * /gs info (uuid) | Gives information about the plot the player is standing on
     * /gs showBorders | Shows the border of every plot
     * /gs listAll | Lists all plots
     * /gs create <name> | Creates a plot with the name
     * /gs delete (uuid) | Deletes the plot with the name
     * /gs tp <uuid> | Teleports to the plot with the uniqueId
     * /gs owner <player> | Sets the owner of the plot
     * /gs member <player> add | Adds a player to the plot
     * /gs member <player> remove | Removes a player from the plot
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
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.already-setting-up")
                                ));
                                return 0;
                            }

                            boolean hasPlot = !this.winterVillage.plotHandler.byOwner(player.getUniqueId()).isEmpty();
                            boolean canBypass = player.hasPermission("wintervillage.plot.ignore_limit");
                            if (hasPlot && !canBypass) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.limit-reached")
                                ));
                                return 0;
                            }

                            SetupTask rectangle = new SetupTask(player);
                            int taskId = rectangle.start();

                            player.getPersistentDataContainer().set(this.winterVillage.plotHandler.plotRectangleKey, PersistentDataType.INTEGER, taskId);
                            player.getPersistentDataContainer().set(this.winterVillage.plotHandler.plotSetupKey, new BoundingBoxDataType(), new BoundingBox2D());
                            player.getInventory().addItem(this.winterVillage.plotHandler.SETUP_ITEM);

                            player.sendMessage(Component.join(
                                    this.winterVillage.prefix,
                                    Component.translatable("wintervillage.commands.plot.setting-up", Component.text(this.winterVillage.plotHandler.MAX_PLOT_WIDTH))
                            ));
                            return 1;
                        })
                )
                .then(Commands.literal("info")
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            Plot plot = this.winterVillage.plotHandler.byBounds(player.getLocation());
                            if (plot == null) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.not-found-by-location")
                                ));
                                return 0;
                            }

                            // TODO: message
                            return 1;
                        })
                )
                .then(Commands.literal("showBorders")
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            if (player.getPersistentDataContainer().has(this.winterVillage.plotHandler.plotBoundariesKey)) {
                                int taskId = player.getPersistentDataContainer().get(this.winterVillage.plotHandler.plotBoundariesKey, PersistentDataType.INTEGER);

                                BoundariesTask task = BoundariesTask.task(taskId);
                                if (task != null) task.stop();

                                player.getPersistentDataContainer().remove(this.winterVillage.plotHandler.plotBoundariesKey);
                                return 1;
                            }

                            BoundariesTask task = new BoundariesTask(player);
                            int taskId = task.start();

                            player.getPersistentDataContainer().set(this.winterVillage.plotHandler.plotBoundariesKey, PersistentDataType.INTEGER, taskId);
                            return 1;
                        }))
                .then(Commands.literal("listAll")
                        .requires((source) -> source.getSender().hasPermission("wintervillage.plot.command.listAll"))
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            if (this.winterVillage.plotHandler.getPlotCache().isEmpty()) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.no-plots-found")
                                ));
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
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.not-setting-up")
                                        ));
                                        return 0;
                                    }

                                    BoundingBox2D boundingBox = container.get(this.winterVillage.plotHandler.plotSetupKey, new BoundingBoxDataType());
                                    if (!boundingBox.isDefined()) {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.configuration-failed")
                                        ));
                                        return 0;
                                    }

                                    boolean tooLarge = (!player.hasPermission("wintervillage.plot.width_bypass")
                                            && (boundingBox.getWidthX() > this.winterVillage.plotHandler.MAX_PLOT_WIDTH
                                            || boundingBox.getWidthZ() > this.winterVillage.plotHandler.MAX_PLOT_WIDTH));
                                    if (tooLarge) {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.too-large")
                                        ));
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
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.plot.created")
                                                ));
                                                this.winterVillage.plotHandler.plotCache.add(plot);
                                            })
                                            .exceptionally(throwable -> {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.plot.failed-to-create", throwable.getMessage())
                                                ));
                                                return null;
                                            });

                                    this.winterVillage.plotHandler.stopTasks(player);
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
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.not-found-by-uniqueId")
                                        ));
                                        return 0;
                                    }

                                    this.winterVillage.plotDatabase.delete(plot.uniqueId())
                                            .thenAccept((v) -> {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.plot.deleted")
                                                ));
                                                this.winterVillage.plotHandler.plotCache.remove(plot);
                                            })
                                            .exceptionally(throwable -> {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.plot.failed-to-delete", throwable.getMessage())
                                                ));
                                                return null;
                                            });

                                    return 1;
                                })
                        )
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            Plot plot = this.winterVillage.plotHandler.byBounds(player.getLocation());
                            if (plot == null) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.not-found-by-location")
                                ));
                                return 0;
                            }

                            boolean notOwner = !plot.owner().equals(player.getUniqueId());
                            boolean canBypass = player.hasPermission("wintervillage.plot.ignore_owner");
                            if (notOwner && !canBypass) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.not-owner")
                                ));
                                return 0;
                            }

                            this.winterVillage.plotDatabase.delete(plot.uniqueId())
                                    .thenAccept((v) -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.deleted")
                                        ));
                                        this.winterVillage.plotHandler.plotCache.remove(plot);
                                    })
                                    .exceptionally(throwable -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.failed-to-delete", throwable.getMessage())
                                        ));
                                        return null;
                                    });

                            return 1;
                        })
                )
                .then(Commands.literal("tp")
                        .then(Commands.argument("uuid", ArgumentTypes.uuid())
                                .executes((source) -> {
                                    final Player player = (Player) source.getSource().getSender();

                                    final UUID uniqueId = source.getArgument("uuid", UUID.class);
                                    Plot plot = this.winterVillage.plotHandler.byUniqueId(uniqueId);
                                    if (plot == null) {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.not-found-by-uniqueId")
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
                                            highestBlockAt,
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
                                    return 1;
                                })))
                .then(Commands.literal("owner")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((source, builder1) -> {
                                    Bukkit.getOnlinePlayers().forEach(player -> builder1.suggest(player.getName()));
                                    return builder1.buildFuture();
                                })
                                .executes((source) -> {
                                    final Player player = (Player) source.getSource().getSender();
                                    final String name = StringArgumentType.getString(source, "name");

                                    Plot plot = this.winterVillage.plotHandler.byBounds(player.getLocation());
                                    if (plot == null) {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.not-found-by-location")
                                        ));
                                        return 0;
                                    }

                                    boolean notOwner = !plot.owner().equals(player.getUniqueId());
                                    boolean canBypass = player.hasPermission("wintervillage.plot.ignore_owner");
                                    if (notOwner && !canBypass) {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.not-owner")
                                        ));
                                        return 0;
                                    }

                                    CompletableFuture<Optional<UUID>> uuidFuture = this.winterVillage.playerHandler.lookupUniqueId(name);
                                    uuidFuture.thenCompose(uuidOptional -> uuidOptional
                                                    .map(uuid -> this.winterVillage.luckPerms.getUserManager().loadUser(uuid, name).thenApply(Optional::of))
                                                    .orElseGet(() -> {
                                                        player.sendMessage(Component.join(this.winterVillage.prefix, Component.translatable("wintervillage.commands.player-not-found")));
                                                        return CompletableFuture.completedFuture(Optional.empty());
                                                    })
                                            )
                                            .thenCompose(userOptional -> userOptional
                                                    .map(user -> {
                                                        if (plot.owner().equals(user.getUniqueId())) {
                                                            Group highestGroup = this.winterVillage.playerHandler.highestGroup(user);
                                                            player.sendMessage(Component.join(this.winterVillage.prefix,
                                                                    Component.translatable("wintervillage.commands.plot.player-is-owner",
                                                                            MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + user.getUsername())
                                                                    )
                                                            ));
                                                            return CompletableFuture.completedFuture(Optional.<CombinedUserPlot>empty());
                                                        }

                                                        return this.winterVillage.plotDatabase.modify(plot.uniqueId(), updated -> updated.owner(user.getUniqueId()))
                                                                .thenApply(updatedPlot -> Optional.of(new CombinedUserPlot(user, updatedPlot)));
                                                    })
                                                    .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
                                            )
                                            .thenAccept(combinedUserPlot -> combinedUserPlot.ifPresent(combined -> {
                                                Group highestGroup = this.winterVillage.playerHandler.highestGroup(combined.user());
                                                player.sendMessage(Component.join(this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.plot.updated-owner",
                                                                MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + combined.user().getUsername())
                                                        )
                                                ));
                                                this.winterVillage.plotHandler.forceUpdate();
                                            }))
                                            .exceptionally(throwable -> {
                                                player.sendMessage(Component.join(this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.plot.failed-to-update", throwable.getMessage())
                                                ));
                                                return null;
                                            });
                                    return 1;
                                })))
                .then(Commands.literal("member")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((source, builder1) -> {
                                    Bukkit.getOnlinePlayers().forEach(player -> builder1.suggest(player.getName()));
                                    return builder1.buildFuture();
                                })
                                .then(Commands.literal("add")
                                        .executes((source) -> {
                                            final Player player = (Player) source.getSource().getSender();
                                            final String name = StringArgumentType.getString(source, "name");

                                            Plot plot = this.winterVillage.plotHandler.byBounds(player.getLocation());
                                            if (plot == null) {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.plot.not-found-by-location")
                                                ));
                                                return 0;
                                            }

                                            boolean notOwner = !plot.owner().equals(player.getUniqueId());
                                            boolean canBypass = player.hasPermission("wintervillage.plot.ignore_owner");
                                            if (notOwner && !canBypass) {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.plot.not-owner")
                                                ));
                                                return 0;
                                            }

                                            CompletableFuture<Optional<UUID>> uuidFuture = this.winterVillage.playerHandler.lookupUniqueId(name);
                                            uuidFuture.thenCompose(uuidOptional -> uuidOptional
                                                            .map(uuid -> this.winterVillage.luckPerms.getUserManager().loadUser(uuid, name).thenApply(Optional::of))
                                                            .orElseGet(() -> {
                                                                player.sendMessage(Component.join(this.winterVillage.prefix, Component.translatable("wintervillage.commands.player-not-found")));
                                                                return CompletableFuture.completedFuture(Optional.empty());
                                                            })
                                                    )
                                                    .thenCompose(userOptional -> userOptional
                                                            .map(user -> {
                                                                Group highestGroup = this.winterVillage.playerHandler.highestGroup(user);

                                                                if (plot.owner().equals(user.getUniqueId())) {
                                                                    player.sendMessage(Component.join(
                                                                            this.winterVillage.prefix,
                                                                            Component.translatable("wintervillage.commands.plot.player-is-owner",
                                                                                    MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + user.getUsername()))
                                                                    ));
                                                                    return CompletableFuture.completedFuture(Optional.<CombinedUserPlot>empty());
                                                                }

                                                                if (plot.members().contains(user.getUniqueId())) {
                                                                    player.sendMessage(Component.join(
                                                                            this.winterVillage.prefix,
                                                                            Component.translatable("wintervillage.commands.plot.already-member",
                                                                                    MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + user.getUsername()))
                                                                    ));
                                                                    return CompletableFuture.completedFuture(Optional.<CombinedUserPlot>empty());
                                                                }

                                                                return this.winterVillage.plotDatabase.modify(plot.uniqueId(), updated -> updated.addMember(user.getUniqueId()))
                                                                        .thenApply(updatedPlot -> Optional.of(new CombinedUserPlot(user, updatedPlot)));
                                                            })
                                                            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
                                                    )
                                                    .thenAccept(combinedUserPlot -> combinedUserPlot.ifPresent(combined -> {
                                                        Group highestGroup = this.winterVillage.playerHandler.highestGroup(combined.user());
                                                        player.sendMessage(Component.join(
                                                                this.winterVillage.prefix,
                                                                Component.translatable("wintervillage.commands.plot.added-member",
                                                                        MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + combined.user().getUsername())
                                                                )
                                                        ));
                                                        this.winterVillage.plotHandler.forceUpdate();
                                                    }))
                                                    .exceptionally(throwable -> {
                                                        player.sendMessage(Component.join(
                                                                this.winterVillage.prefix,
                                                                Component.translatable("wintervillage.commands.plot.failed-to-update", throwable.getMessage())
                                                        ));
                                                        return null;
                                                    });
                                            return 1;
                                        }))
                                .then(Commands.literal("remove")
                                        .executes((source) -> {
                                            final Player player = (Player) source.getSource().getSender();
                                            final String name = StringArgumentType.getString(source, "name");

                                            Plot plot = this.winterVillage.plotHandler.byBounds(player.getLocation());
                                            if (plot == null) {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.plot.not-found-by-location")
                                                ));
                                                return 0;
                                            }

                                            boolean notOwner = !plot.owner().equals(player.getUniqueId());
                                            boolean canBypass = player.hasPermission("wintervillage.plot.ignore_owner");
                                            if (notOwner && !canBypass) {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.plot.not-owner")
                                                ));
                                                return 0;
                                            }

                                            CompletableFuture<Optional<UUID>> uuidFuture = this.winterVillage.playerHandler.lookupUniqueId(name);
                                            uuidFuture.thenCompose(uuidOptional -> uuidOptional
                                                            .map(uuid -> this.winterVillage.luckPerms.getUserManager().loadUser(uuid, name).thenApply(Optional::of))
                                                            .orElseGet(() -> {
                                                                player.sendMessage(Component.join(this.winterVillage.prefix, Component.translatable("wintervillage.commands.player-not-found")));
                                                                return CompletableFuture.completedFuture(Optional.empty());
                                                            })
                                                    )
                                                    .thenCompose(userOptional -> userOptional
                                                            .map(user -> {
                                                                Group highestGroup = this.winterVillage.playerHandler.highestGroup(user);

                                                                if (!plot.members().contains(user.getUniqueId())) {
                                                                    player.sendMessage(Component.join(
                                                                            this.winterVillage.prefix,
                                                                            Component.translatable("wintervillage.commands.plot.not-member",
                                                                                    MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + user.getUsername()))
                                                                    ));
                                                                    return CompletableFuture.completedFuture(Optional.<CombinedUserPlot>empty());
                                                                }

                                                                return this.winterVillage.plotDatabase.modify(plot.uniqueId(), updated -> updated.removeMember(user.getUniqueId()))
                                                                        .thenApply(updatedPlot -> Optional.of(new CombinedUserPlot(user, updatedPlot)));
                                                            })
                                                            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
                                                    )
                                                    .thenAccept(combinedUserPlot -> combinedUserPlot.ifPresent(combined -> {
                                                        Group highestGroup = this.winterVillage.playerHandler.highestGroup(combined.user());
                                                        player.sendMessage(Component.join(
                                                                this.winterVillage.prefix,
                                                                Component.translatable("wintervillage.commands.plot.removed-member",
                                                                        MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + combined.user().getUsername())
                                                                )
                                                        ));
                                                        this.winterVillage.plotHandler.forceUpdate();
                                                    }))
                                                    .exceptionally(throwable -> {
                                                        player.sendMessage(Component.join(
                                                                this.winterVillage.prefix,
                                                                Component.translatable("wintervillage.commands.plot.failed-to-update", throwable.getMessage())
                                                        ));
                                                        return null;
                                                    });
                                            return 1;
                                        })))
                );
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Manage your plots", List.of());
    }
}
