package de.wintervillage.proxy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.core.player.data.WhitelistInformation;
import de.wintervillage.common.core.type.Pair;
import de.wintervillage.proxy.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.user.User;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WhitelistCommand {

    /**
     * /whitelist add <name>
     * /whitelist remove <name>
     * TODO: /whitelist list
     */

    private final WinterVillage winterVillage;

    private final PlayerManager playerManager;

    public WhitelistCommand(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;

        ServiceRegistry serviceRegistry = InjectionLayer.ext().instance(ServiceRegistry.class);
        this.playerManager = serviceRegistry.firstProvider(PlayerManager.class);
    }

    public BrigadierCommand create() {
        LiteralCommandNode<CommandSource> node = BrigadierCommand.literalArgumentBuilder("whitelist")
                .then(BrigadierCommand.literalArgumentBuilder("remove")
                        .requires(context -> context.hasPermission("wintervillage.command.whitelist.remove"))
                        .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                                .executes(context -> {
                                    final String playerName = context.getArgument("player", String.class);

                                    this.winterVillage.playerHandler.lookupUniqueId(playerName)
                                            .thenCompose(uniqueId -> this.winterVillage.playerHandler.combinedPlayer(uniqueId, playerName)) // loading the player entered in "playerName"
                                            .thenCompose(receiver -> {
                                                if (context.getSource() instanceof Player player) {
                                                    return this.winterVillage.playerHandler.combinedPlayer(player.getUniqueId(), player.getUsername())
                                                            .thenApply(executor -> Pair.of(receiver, executor));
                                                } else
                                                    return CompletableFuture.completedFuture(Pair.of(receiver, null)); // combine into Pair(Receiver, Executor)
                                            })
                                            .thenCompose(pair -> {
                                                WinterVillagePlayer receiver = pair.first().second();

                                                if (receiver.whitelistInformation() == null) {
                                                    context.getSource().sendMessage(Component.join(
                                                            this.winterVillage.prefix,
                                                            Component.translatable("wintervillage.command.whitelist.player-not-whitelisted",
                                                                    MiniMessage.miniMessage().deserialize(pair.first().first().getCachedData().getMetaData().getMetaValue("color") + playerName)
                                                            )
                                                    ));
                                                    return CompletableFuture.completedFuture(null);
                                                }

                                                return this.winterVillage.playerDatabase.modify(receiver.uniqueId(), builder -> builder.whitelistInformation(null))
                                                        .thenApply(_ -> pair.first().first());
                                            })
                                            .thenAccept(user -> {
                                                if (user == null) return;

                                                context.getSource().sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.command.whitelist.player-removed",
                                                                MiniMessage.miniMessage().deserialize(user.getCachedData().getMetaData().getMetaValue("color") + playerName)
                                                        )
                                                ));
                                            })
                                            .exceptionally(throwable -> {
                                                context.getSource().sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.command.whitelist.failed", Component.text(throwable.getMessage()))
                                                ));
                                                return null;
                                            });
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(BrigadierCommand.literalArgumentBuilder("add")
                        .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                                .executes(context -> {
                                    final String playerName = context.getArgument("player", String.class);

                                    this.winterVillage.playerHandler.lookupUniqueId(playerName)
                                            .thenCompose(uniqueId -> this.winterVillage.playerHandler.combinedPlayer(uniqueId, playerName)) // loading the player entered in "playerName"
                                            .thenCompose(receiver -> {
                                                if (context.getSource() instanceof Player player) {
                                                    return this.winterVillage.playerHandler.combinedPlayer(player.getUniqueId(), player.getUsername())
                                                            .thenApply(executor -> Pair.of(receiver, executor));
                                                } else
                                                    return CompletableFuture.completedFuture(Pair.of(receiver, null)); // combine into Pair(Receiver, Executor)
                                            })
                                            .thenCompose(pair -> {
                                                WinterVillagePlayer receiver = pair.first().second();

                                                if (receiver.whitelistInformation() != null) {
                                                    context.getSource().sendMessage(Component.join(
                                                            this.winterVillage.prefix,
                                                            Component.translatable("wintervillage.command.whitelist.player-already-whitelisted",
                                                                    MiniMessage.miniMessage().deserialize(pair.first().first().getCachedData().getMetaData().getMetaValue("color") + playerName)
                                                            )
                                                    ));
                                                    return CompletableFuture.completedFuture(null);
                                                }

                                                if (pair.second() == null) {
                                                    return this.winterVillage.playerDatabase.modify(receiver.uniqueId(), builder -> builder.whitelistInformation(new WhitelistInformation(new UUID(0L, 0L), System.currentTimeMillis())))
                                                            .thenApply(_ -> pair.first().first());
                                                }

                                                Pair<User, WinterVillagePlayer> executor = (Pair<User, WinterVillagePlayer>) pair.second();
                                                if (executor.second().wildcardInformation().amount() < 1 && !context.getSource().hasPermission("wintervillage.command.bypass-whitelist")) {
                                                    context.getSource().sendMessage(Component.join(
                                                            this.winterVillage.prefix,
                                                            Component.translatable("wintervillage.command.whitelist.not-enough-wildcards")
                                                    ));
                                                    return CompletableFuture.completedFuture(null);
                                                }

                                                return this.winterVillage.playerDatabase.modify(receiver.uniqueId(), builder -> builder.whitelistInformation(
                                                        new WhitelistInformation(executor.second().uniqueId(), System.currentTimeMillis())
                                                )).thenApply(_ -> pair.first().first());
                                            })
                                            .thenAccept(user -> {
                                                if (user == null) return;

                                                context.getSource().sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.command.whitelist.player-added",
                                                                MiniMessage.miniMessage().deserialize(user.getCachedData().getMetaData().getMetaValue("color") + playerName)
                                                        )
                                                ));
                                            })
                                            .exceptionally(throwable -> {
                                                context.getSource().sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.command.whitelist.failed", Component.text(throwable.getMessage()))
                                                ));
                                                return null;
                                            });
                                    return Command.SINGLE_SUCCESS;
                                }))).build();
        return new BrigadierCommand(node);
    }
}
