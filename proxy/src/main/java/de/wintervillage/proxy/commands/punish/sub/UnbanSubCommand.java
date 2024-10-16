package de.wintervillage.proxy.commands.punish.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.user.User;

import java.util.concurrent.CompletableFuture;

public class UnbanSubCommand {

    private final WinterVillage winterVillage;

    private final PlayerManager playerManager;

    public UnbanSubCommand(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;

        ServiceRegistry serviceRegistry = InjectionLayer.ext().instance(ServiceRegistry.class);
        this.playerManager = serviceRegistry.firstProvider(PlayerManager.class);
    }

    public LiteralCommandNode<CommandSource> create() {
        LiteralCommandNode<CommandSource> node = BrigadierCommand.literalArgumentBuilder("unban")
                .then(BrigadierCommand.requiredArgumentBuilder("playerName", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            this.playerManager.onlinePlayers().names().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            this.handle(context);
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
        return new BrigadierCommand(node).getNode();
    }

    private void handle(CommandContext<CommandSource> context) {
        String playerName = context.getArgument("playerName", String.class);

        this.winterVillage.playerHandler.lookupUniqueId(playerName)
                .thenCompose(uniqueId -> this.winterVillage.playerHandler.combinedPlayer(uniqueId, playerName)) // loads the player that gets punished
                .thenCompose(punishedPlayer -> {
                    if (context.getSource() instanceof Player player) {
                        return this.winterVillage.playerHandler.combinedPlayer(player.getUniqueId(), player.getUsername())
                                .thenApply(punisher -> Pair.of(punishedPlayer, punisher));
                    } else return CompletableFuture.completedFuture(Pair.of(punishedPlayer, null));
                })
                .exceptionally(throwable -> {
                    context.getSource().sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.commands.player-not-found")
                    ));
                    return null;
                })
                .thenCompose(pair -> {
                    Pair<User, WinterVillagePlayer> punished = pair.first();

                    if (punished.second().banInformation() == null) {
                        context.getSource().sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.command.punish.error.removal-failed",
                                        MiniMessage.miniMessage().deserialize(punished.first().getCachedData().getMetaData().getMetaValue("color") + playerName)
                                )
                        ));
                        return CompletableFuture.completedFuture(null);
                    }

                    return this.winterVillage.playerDatabase.modify(punished.second().uniqueId(), builder -> builder.banInformation(null))
                            .thenApply(_ -> pair.first().first()); // return User
                })
                .thenAccept(punished -> {
                    if (punished == null) return;

                    context.getSource().sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.command.punish.punished-unban",
                                    MiniMessage.miniMessage().deserialize(punished.getCachedData().getMetaData().getMetaValue("color") + playerName)
                            )
                    ));
                });
    }
}
