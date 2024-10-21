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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.user.User;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WhitelistCommand {

    /**
     * /whitelist add <name>
     * /whitelist remove <name>
     * /whitelist list
     */

    private final WinterVillage winterVillage;

    public WhitelistCommand(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;
    }

    public BrigadierCommand create() {
        LiteralCommandNode<CommandSource> node = BrigadierCommand.literalArgumentBuilder("whitelist")
                .then(BrigadierCommand.literalArgumentBuilder("list")
                        .requires(context -> context.hasPermission("wintervillage.command.whitelist.list"))
                        .executes(context -> {
                            this.winterVillage.playerDatabase.players()
                                    .thenCompose(collection -> {
                                        Collection<CompletableFuture<Pair<User, WinterVillagePlayer>>> futures = collection.stream()
                                                .map(player -> this.winterVillage.playerHandler.combinedPlayer(player.uniqueId(), null))
                                                .toList();

                                        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                                .thenApply(v -> futures.stream()
                                                        .map(CompletableFuture::join)
                                                        .collect(Collectors.toList())
                                                );
                                    })
                                    .thenAccept(test -> {
                                        List<Pair<User, WinterVillagePlayer>> players = test.stream()
                                                .filter(pair -> pair.second().whitelistInformation() != null)
                                                .toList();

                                        context.getSource().sendMessage(Component.translatable("wintervillage.command.whitelist.list-header", Component.text(players.size())));

                                        List<Component> components = players.stream()
                                                .map(pair -> MiniMessage.miniMessage().deserialize(pair.first().getCachedData().getMetaData().getMetaValue("color") + pair.first().getUsername()))
                                                .toList();
                                        context.getSource().sendMessage(Component.join(
                                                JoinConfiguration.separator(Component.text(", ", NamedTextColor.DARK_GRAY)),
                                                components
                                        ));
                                    })
                                    .exceptionally(throwable -> {
                                        context.getSource().sendMessage(Component.text("Failed: " + throwable.getMessage()));
                                        return null;
                                    });
                            return Command.SINGLE_SUCCESS;
                        })
                )
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
                        .requires(context -> context.hasPermission("wintervillage.command.whitelist.add"))
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
                                                if (executor.second().wildcardInformation().currentAmount() < 1 && !context.getSource().hasPermission("wintervillage.command.bypass-whitelist")) {
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
