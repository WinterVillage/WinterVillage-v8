package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import de.wintervillage.main.plot.combined.CombinedUserPlot;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MemberSubCommand {

    private final WinterVillage winterVillage;

    public MemberSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("member")
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
                                    return Command.SINGLE_SUCCESS;
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
                                    return Command.SINGLE_SUCCESS;
                                })));
    }
}
