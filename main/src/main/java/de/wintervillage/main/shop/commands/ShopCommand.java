package de.wintervillage.main.shop.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ShopCommand {

    /**
     * /shop create <name> <price> | Starts the setup and gives the player an axe to select the corners
     */

    public ShopCommand(Commands commands) {
        WinterVillage winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("shop")
                .requires((source) -> source.getSender() instanceof Player)
                .then(Commands.literal("refresh")
                        .requires((source) -> source.getSender().hasPermission("wintervillage.command.shop.refresh"))
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            winterVillage.shopHandler.shops().forEach(Shop::updateInformation);

                            return Command.SINGLE_SUCCESS;
                        }));
        commands.register(winterVillage.getPluginMeta(), builder.build(), "Manage shops", List.of());
    }
}
