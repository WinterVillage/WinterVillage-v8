package de.wintervillage.main.shop.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
                                .executes((source) -> this.handle(source, true))
                        )
                        .executes((source) -> this.handle(source, false))
                );
    }

    private int handle(CommandContext<CommandSourceStack> context, boolean hasUniqueId) {
        final Player player = (Player) context.getSource().getSender();
        BigDecimal newPrice = context.getArgument("newprice", BigDecimal.class);

        Optional<Shop> optional = hasUniqueId
                ? this.winterVillage.shopHandler.byUniqueId(context.getArgument("uniqueId", UUID.class))
                : this.winterVillage.shopHandler.raytrace(player);
        if (optional.isEmpty()) {
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.commands.shop.not-found-by-" + (hasUniqueId ? "uniqueId" : "raytrace"))
            ));
            return 0;
        }

        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.commands.shop.price-too-low")
            ));
            return 0;
        }

        Shop shop = optional.get();
        this.winterVillage.shopDatabase.modify(shop.uniqueId(), consumer -> consumer.price(newPrice))
                .thenAccept(updatedShop -> {
                    player.sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.commands.shop.changed-price",
                                    Component.text(newPrice.toPlainString())
                            )
                    ));

                    Bukkit.getScheduler().runTask(this.winterVillage, () -> {
                        shop.price(updatedShop.price());
                        shop.updateInformation();
                    });
                })
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
    }
}
