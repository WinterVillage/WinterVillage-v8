package de.wintervillage.main.shop.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import de.wintervillage.main.shop.combined.CombinedUserShop;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChangeOwnerSubCommand {

    private final WinterVillage winterVillage;

    public ChangeOwnerSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("changeowner")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();
                            String name = StringArgumentType.getString(source, "name");

                            Optional<Shop> optional = this.winterVillage.shopHandler.raytrace(player);
                            if (optional.isEmpty()) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.shop.not-found-by-raytrace")
                                ));
                                return 0;
                            }

                            Shop shop = optional.get();

                            boolean notOwner = !shop.owner().equals(player.getUniqueId());
                            boolean canBypass = player.hasPermission("wintervillage.shop.ignore_owner");
                            if (notOwner && !canBypass) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.shop.not-owner")
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

                                                if (shop.owner().equals(user.getUniqueId())) {
                                                    player.sendMessage(Component.join(
                                                            this.winterVillage.prefix,
                                                            Component.translatable("wintervillage.commands.shop.player-is-owner",
                                                                    MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + user.getUsername()))
                                                    ));
                                                    return CompletableFuture.completedFuture(Optional.<CombinedUserShop>empty());
                                                }

                                                return this.winterVillage.shopDatabase.modify(shop.uniqueId(), updated -> updated.owner(user.getUniqueId()))
                                                        .thenApply(updatedShop -> Optional.of(new CombinedUserShop(user, updatedShop)));
                                            })
                                            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
                                    )
                                    .thenAccept(updatedShop -> updatedShop.ifPresent(combined -> {
                                                Group highestGroup = this.winterVillage.playerHandler.highestGroup(combined.owner());
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.shop.updated-owner",
                                                                MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + combined.owner().getUsername())
                                                        )
                                                ));

                                                shop.owner(updatedShop.get().shop().owner());
                                            })
                                    )
                                    .exceptionally(throwable -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.shop.failed-to-update",
                                                        Component.text(throwable.getMessage())
                                                )
                                        ));
                                        return null;
                                    });
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
