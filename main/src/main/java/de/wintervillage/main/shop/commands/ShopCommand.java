package de.wintervillage.main.shop.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.commands.sub.ChangePriceSubCommand;
import de.wintervillage.main.shop.commands.sub.DeleteSubCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ShopCommand {

    /**
     * /shop delete (uniqueId) | Deletes a shop
     * /shop changePrice <newPrice> (uniqueId) | Changes the price of a shop
     *
     */

    public ShopCommand(Commands commands) {
        WinterVillage winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("shop")
                .requires((source) -> source.getSender() instanceof Player)
                .then(new DeleteSubCommand().build())
                .then(new ChangePriceSubCommand().build());
        commands.register(winterVillage.getPluginMeta(), builder.build(), "Manage shops", List.of());
    }
}
