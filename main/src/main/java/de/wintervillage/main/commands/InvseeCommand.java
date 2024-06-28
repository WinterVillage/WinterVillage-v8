package de.wintervillage.main.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class InvseeCommand {

    private final WinterVillage winterVillage;

    public InvseeCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        // TODO: LuckPerms
        final LiteralArgumentBuilder builder = Commands.literal("invsee")
                .requires((source) -> source.getSender() instanceof Player)
                .then(
                        Commands.argument("player", ArgumentTypes.player())
                                .executes((source) -> {
                                    Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(source.getSource()).get(0);

                                    ((Player) source.getSource().getExecutor()).openInventory(player.getInventory());
                                    ((Player) source.getSource().getExecutor()).playSound(((Player) source.getSource().getExecutor()).getLocation(), Sound.BLOCK_CHEST_OPEN, 3.0f, 3.0f);
                                    return 1;
                                })
                );
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Opens the inventory of the player you want", List.of());
    }

}
