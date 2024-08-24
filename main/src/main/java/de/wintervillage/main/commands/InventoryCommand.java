package de.wintervillage.main.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class InventoryCommand {

    private final WinterVillage winterVillage;

    public InventoryCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        final LiteralArgumentBuilder builder = Commands.literal("inventory")
                .requires((source) -> source.getSender() instanceof Player player && player.hasPermission("wintervillage.command.inventory"))
                .then(Commands.literal("normal")
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .executes((source) -> {
                                    Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(source.getSource()).get(0);

                                    ((Player) source.getSource().getSender()).openInventory(player.getInventory());
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("enderchest")
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .executes((source) -> {
                                    Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(source.getSource()).get(0);

                                    ((Player) source.getSource().getSender()).openInventory(player.getEnderChest());
                                    return 1;
                                })
                        )
                );
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Opens the inventory of the player you want", List.of());
    }

}
