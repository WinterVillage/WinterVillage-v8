package de.wintervillage.proxy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.core.type.Pair;
import de.wintervillage.proxy.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.user.User;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class TransferCommand {

    /**
     * /transfer <name> <sum>
     */

    private final WinterVillage winterVillage;

    private final PlayerManager playerManager;

    public TransferCommand(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;

        ServiceRegistry serviceRegistry = InjectionLayer.ext().instance(ServiceRegistry.class);
        this.playerManager = serviceRegistry.firstProvider(PlayerManager.class);
    }

    public BrigadierCommand create() {
        LiteralCommandNode<CommandSource> node = BrigadierCommand.literalArgumentBuilder("transfer")
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                        .then(BrigadierCommand.requiredArgumentBuilder("sum", StringArgumentType.string())
                                .executes(context -> {
                                    final String playerName = context.getArgument("player", String.class);
                                    final String sumString = context.getArgument("sum", String.class);

                                    // goofy velocity can't handle custom arguments
                                    BigDecimal sum;
                                    try {
                                        sum = new BigDecimal(sumString);
                                    } catch (NumberFormatException exception) {
                                        context.getSource().sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.command.transfer.invalid-decimal")
                                        ));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if (sum.compareTo(BigDecimal.ZERO) < 0) {
                                        context.getSource().sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.command.transfer.sum-to-small")
                                        ));
                                        return Command.SINGLE_SUCCESS;
                                    }

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

                                                if (pair.second() == null) {
                                                    return this.winterVillage.playerDatabase.modify(receiver.uniqueId(), builder -> builder.money(builder.money().add(sum)))
                                                            .thenApply(_ -> Pair.of(pair.first().first(), null));
                                                }

                                                Pair<User, WinterVillagePlayer> executor = (Pair<User, WinterVillagePlayer>) pair.second();
                                                if (executor.second().money().compareTo(sum) < 0) {
                                                    context.getSource().sendMessage(Component.join(
                                                            this.winterVillage.prefix,
                                                            Component.translatable("wintervillage.command.transfer.not-enough-money")
                                                    ));
                                                    return CompletableFuture.completedFuture(null);
                                                }

                                                CompletableFuture<WinterVillagePlayer> executorFuture = this.winterVillage.playerDatabase.modify(executor.first().getUniqueId(), builder -> builder.money(builder.money().subtract(sum)));
                                                CompletableFuture<WinterVillagePlayer> receiverFuture = this.winterVillage.playerDatabase.modify(receiver.uniqueId(), builder -> builder.money(builder.money().add(sum)));

                                                return executorFuture.thenCombine(receiverFuture, (_, _) -> Pair.of(pair.first().first(), executor.first())); // return Pair(User, User)
                                            })
                                            .thenAccept(pair -> {
                                                if (pair == null) return;

                                                if (this.playerManager.onlinePlayer(pair.first().getUniqueId()) != null) {
                                                    Component sender = pair.second() == null ?
                                                            Component.text("Server", NamedTextColor.RED) :
                                                            MiniMessage.miniMessage().deserialize(((User) pair.second()).getCachedData().getMetaData().getMetaValue("color") + ((User) pair.second()).getUsername());

                                                    this.playerManager.playerExecutor(pair.first().getUniqueId()).sendChatMessage(Component.join(
                                                            this.winterVillage.prefix,
                                                            Component.translatable("wintervillage.command.transfer.success-receiver",
                                                                    Component.text(this.formatBD(sum, true)),
                                                                    sender
                                                            )
                                                    ));
                                                }

                                                context.getSource().sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.command.transfer.success-sender",
                                                                MiniMessage.miniMessage().deserialize(pair.first().getCachedData().getMetaData().getMetaValue("color") + playerName),
                                                                Component.text(this.formatBD(sum, true))
                                                        )
                                                ));
                                            })
                                            .exceptionally(throwable -> {
                                                context.getSource().sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.command.transfer.failed", Component.text(throwable.getMessage()))
                                                ));
                                                return null;
                                            });
                                    return Command.SINGLE_SUCCESS;
                                }))).build();
        return new BrigadierCommand(node);
    }

    private String formatBD(BigDecimal bigDecimal, boolean fractions) {
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
        if (fractions) {
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setMinimumFractionDigits(2);
        }
        numberFormat.setGroupingUsed(true);
        return numberFormat.format(bigDecimal);
    }
}
