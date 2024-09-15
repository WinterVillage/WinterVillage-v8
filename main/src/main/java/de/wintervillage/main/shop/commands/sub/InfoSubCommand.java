package de.wintervillage.main.shop.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

public class InfoSubCommand {

    private final WinterVillage winterVillage;

    public InfoSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                .then(Commands.argument("uniqueId", ArgumentTypes.uuid())
                        .requires((source) -> source.getSender().hasPermission("wintervillage.shop.command.info_by_id"))
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();
                            final UUID uniqueId = source.getArgument("uniqueId", UUID.class);

                            Optional<Shop> optional = this.winterVillage.shopHandler.byUniqueId(uniqueId);
                            if (optional.isEmpty()) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.shop.not-found-by-uniqueId",
                                                Component.text(uniqueId.toString())
                                        )
                                ));
                                return 0;
                            }

                            Shop shop = optional.get();

                            this.winterVillage.luckPerms.getUserManager().loadUser(shop.owner())
                                    .thenAccept(user -> {
                                        Group highestGroup = this.winterVillage.playerHandler.highestGroup(user);

                                        player.sendMessage(Component.translatable(
                                                "wintervillage.commands.shop.info",
                                                Component.text(shop.name()), // <arg:0>
                                                MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + user.getUsername()), // <arg:2>
                                                shop.item() != null ? Component.translatable(shop.item().getType().translationKey()) : Component.text("kein Item eingestellt"), // <arg:3>
                                                Component.text(this.winterVillage.formatBD(shop.price(), true)), // <arg:4>
                                                Component.text(this.winterVillage.formatBD(shop.amount(), false)), // <arg:5>
                                                Component.text(this.winterVillage.formatBD(shop.statistics().earned(), false)), // <arg:6>
                                                Component.text(this.winterVillage.formatBD(shop.statistics().sold(), true)) // <arg:7>
                                        ));
                                    });
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

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

                    this.winterVillage.luckPerms.getUserManager().loadUser(shop.owner())
                            .thenAccept(user -> {
                                Group highestGroup = this.winterVillage.playerHandler.highestGroup(user);

                                player.sendMessage(Component.translatable(
                                        "wintervillage.commands.shop.info",
                                        Component.text(shop.name()), // <arg:0>
                                        MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + user.getUsername()), // <arg:2>
                                        shop.item() != null ? Component.translatable(shop.item().getType().translationKey()) : Component.text("kein Item eingestellt"), // <arg:3>
                                        Component.text(this.winterVillage.formatBD(shop.price(), true)), // <arg:4>
                                        Component.text(this.winterVillage.formatBD(shop.amount(), false)), // <arg:5>
                                        Component.text(this.winterVillage.formatBD(shop.statistics().earned(), false)), // <arg:6>
                                        Component.text(this.winterVillage.formatBD(shop.statistics().sold(), true)) // <arg:7>
                                ));
                            });
                    return Command.SINGLE_SUCCESS;
                });
    }
}
