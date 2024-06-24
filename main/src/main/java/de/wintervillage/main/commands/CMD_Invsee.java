package de.wintervillage.main.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CMD_Invsee {

    private final WinterVillage winterVillage;

    public CMD_Invsee(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.register(commands);
    }

    public void register(Commands commands){
        LiteralArgumentBuilder<CommandSourceStack> invsee_builder = Commands.literal("invsee")
                .then(
                        Commands.argument("player", ArgumentTypes.player())
                                .executes((source) -> {
                                    if(!(source.getSource().getExecutor() instanceof Player executor)){
                                        source.getSource().getExecutor().sendMessage(this.winterVillage.PREFIX + "Dieser Command ist nur durch einen Spieler ausführbar.");
                                        return 0;
                                    }

                                    Player player = source.getArgument("player", Player.class);

                                    if(!Bukkit.getOnlinePlayers().contains(player)){
                                        executor.sendMessage(this.winterVillage.PREFIX + "Dieser Spieler ist nicht online.");
                                        return 0;
                                    }

                                    executor.openInventory(player.getInventory());
                                    executor.playSound(executor.getLocation(), Sound.BLOCK_CHEST_OPEN, 3.0f, 3.0f);

                                    return 1;
                                })
                );

    }

}
