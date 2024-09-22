package de.wintervillage.proxy.commands.punish.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.wintervillage.common.core.player.combined.CombinedPlayer;
import de.wintervillage.proxy.WinterVillage;
import de.wintervillage.proxy.combined.PlayerPair;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class KickSubCommand {

    private final WinterVillage winterVillage;

    private final PlayerManager playerManager;

    public KickSubCommand(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;

        ServiceRegistry serviceRegistry = InjectionLayer.ext().instance(ServiceRegistry.class);
        this.playerManager = serviceRegistry.firstProvider(PlayerManager.class);
    }

    public LiteralCommandNode<CommandSource> create() {
        LiteralCommandNode<CommandSource> node = BrigadierCommand.literalArgumentBuilder("kick")
                .then(BrigadierCommand.requiredArgumentBuilder("playerName", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            this.playerManager.onlinePlayers().names().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder("reason", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String reason = context.getArgument("reason", String.class);
                                    this.handle(context, reason);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .executes(context -> {
                            this.handle(context, null);
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
        return new BrigadierCommand(node).getNode();
    }

    private void kick(CombinedPlayer punished, String reason) {
        if (reason == null)
            this.playerManager.playerExecutor(punished.user().getUniqueId()).kick(Component.translatable("wintervillage.punish.kicked.no-reason"));
        else
            this.playerManager.playerExecutor(punished.user().getUniqueId()).kick(Component.translatable("wintervillage.punish.kicked.reason", Component.text(reason)));
    }

    private void handle(CommandContext<CommandSource> context, @Nullable String reason) {
        String playerName = context.getArgument("playerName", String.class);

        this.winterVillage.playerHandler.lookupUniqueId(playerName)
                .thenCompose(uniqueId -> this.winterVillage.playerHandler.combinedPlayer(uniqueId, playerName)) // loads the player that gets punished
                .thenCompose(punishedPlayer -> {
                    if (context.getSource() instanceof Player player) {
                        return this.winterVillage.playerHandler.combinedPlayer(player.getUniqueId(), player.getUsername())
                                .thenApply(punisher -> new PlayerPair(punishedPlayer, punisher));
                    } else return CompletableFuture.completedFuture(new PlayerPair(punishedPlayer, null));
                })
                .thenAccept(playerPair -> {
                    if (!this.isPunishable(context.getSource(), playerPair, playerName)) return;

                    this.kick(playerPair.first(), reason);
                    context.getSource().sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.commands.punish.punished-kick",
                                    MiniMessage.miniMessage().deserialize(playerPair.first().user().getCachedData().getMetaData().getMetaValue("color") + playerName)
                            )
                    ));
                })
                .exceptionally(throwable -> {
                    context.getSource().sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.commands.player-not-found")
                    ));
                    return null;
                });
    }

    private boolean isPunishable(CommandSource context, PlayerPair playerPair, String playerName) {
        CombinedPlayer punishedPlayer = playerPair.first();
        Group punishedGroup = this.winterVillage.playerHandler.highestGroup(punishedPlayer.user());

        if (playerPair.second() != null) {
            CombinedPlayer punisher = playerPair.second();
            Group punisherGroup = this.winterVillage.playerHandler.highestGroup(punisher.user());

            if (punishedGroup.getWeight().getAsInt() > punisherGroup.getWeight().getAsInt()) {
                context.sendMessage(Component.join(
                        this.winterVillage.prefix,
                        Component.translatable("wintervillage.commands.punish.group-weight-too-low",
                                MiniMessage.miniMessage().deserialize(punishedGroup.getCachedData().getMetaData().getMetaValue("color") + playerName)
                        )
                ));
                return false;
            }
        }
        return true;
    }
}
