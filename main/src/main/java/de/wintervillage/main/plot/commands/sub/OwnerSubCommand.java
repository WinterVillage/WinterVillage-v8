package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.common.core.type.Pair;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OwnerSubCommand {

    private final WinterVillage winterVillage;

    public OwnerSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("owner")
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
                                                    return CompletableFuture.completedFuture(Optional.<Pair<User, Plot>>empty());
                                                }

                                                return this.winterVillage.plotDatabase.modify(plot.uniqueId(), updated -> updated.owner(user.getUniqueId()))
                                                        .thenApply(updatedPlot -> Optional.of(Pair.of(user, updatedPlot)));
                                            })
                                            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
                                    )
                                    .thenAccept(optional -> optional.ifPresent(pair -> {
                                        User user = (User) pair.first();

                                        Group highestGroup = this.winterVillage.playerHandler.highestGroup(user);
                                        player.sendMessage(Component.join(this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.updated-owner",
                                                        MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + user.getUsername())
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
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
