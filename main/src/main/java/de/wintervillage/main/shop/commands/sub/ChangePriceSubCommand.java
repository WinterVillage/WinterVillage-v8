package de.wintervillage.main.shop.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import de.wintervillage.main.shop.commands.argument.BigDecimalArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class ChangePriceSubCommand {

    private final WinterVillage winterVillage;

    public ChangePriceSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("changeprice")
                .then(Commands.argument("newprice", BigDecimalArgumentType.bigDecimal())
                        .then(Commands.argument("uniqueId", ArgumentTypes.uuid())
                                .requires((source) -> source.getSender().hasPermission("wintervillage.shop.command.force_changeprice"))
                                .executes((source) -> {
                                    final Player player = (Player) source.getSource().getSender();
                                    UUID uniqueId = source.getArgument("uniqueId", UUID.class);
                                    BigDecimal newPrice = source.getArgument("newprice", BigDecimal.class);

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
                                    this.winterVillage.shopDatabase.modify(shop.uniqueId(), consumer -> consumer.price(newPrice))
                                            .thenAccept((v) -> {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.shop.changed-price",
                                                                Component.text(newPrice.toPlainString())
                                                        )
                                                ));

                                                Bukkit.getScheduler().runTask(this.winterVillage, () -> {
                                                    shop.price(newPrice);
                                                    shop.updateInformation();
                                                });
                                            })
                                            .exceptionally(throwable -> {
                                                player.sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.commands.shop.failed-to-delete",
                                                                Component.text(throwable.getMessage())
                                                        )
                                                ));
                                                return null;
                                            });
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();
                            BigDecimal newPrice = source.getArgument("newprice", BigDecimal.class);

                            Optional<Shop> optional = this.winterVillage.shopHandler.raytrace(player);
                            if (optional.isEmpty()) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.shop.not-found-by-raytrace")
                                ));
                                return 0;
                            }

                            Shop shop = optional.get();
                            this.winterVillage.shopDatabase.modify(shop.uniqueId(), consumer -> consumer.price(newPrice))
                                    .thenAccept((v) -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.shop.changed-price",
                                                        Component.text(newPrice.toPlainString())
                                                )
                                        ));

                                        Bukkit.getScheduler().runTask(this.winterVillage, () -> {
                                            shop.price(newPrice);
                                            shop.updateInformation();
                                        });
                                    })
                                    .exceptionally(throwable -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.shop.failed-to-delete",
                                                        Component.text(throwable.getMessage())
                                                )
                                        ));
                                        return null;
                                    });
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
