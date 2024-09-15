package de.wintervillage.main.antifreezle.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CMD_AntiFreezle {

    private WinterVillage winterVillage;

    public CMD_AntiFreezle(Commands commands){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.register(commands);
    }

    public void register(Commands commands){
        LiteralArgumentBuilder<CommandSourceStack> builder_antifreezle = Commands.literal("antifreezle")
                .requires((source) -> source.getSender().hasPermission("wintervillage.command.antifreezle"))
                .then(
                        Commands.argument("anti_player", ArgumentTypes.player())
                                .executes((source) -> {
                                    if(!(source.getSource().getExecutor() instanceof Player player)){
                                        source.getSource().getSender().sendMessage(this.winterVillage.PREFIX + "Dieser Command ist nur durch einen Spieler ausführbar.");
                                        return 0;
                                    }

                                    Player anti_player = source.getArgument("anti_player", Player.class);

                                    if(player.getName().equalsIgnoreCase("Shinigami_MC") && !anti_player.getName().equalsIgnoreCase("FreezleYT")){
                                        player.sendMessage(this.winterVillage.PREFIX + "Du kannst nur das Anti-Inventar von FreezleYT öffnen (kek).");
                                        return 0;
                                    }

                                    if(!player.getName().equalsIgnoreCase("Shinigami_MC") && !player.getName().equalsIgnoreCase("Schmolldechse")
                                        && !player.getName().equalsIgnoreCase("_Eagler_")){
                                        player.sendMessage(this.winterVillage.PREFIX + "Du kannst das Anti-Inventar nicht öffnen.");
                                        return 0;
                                    }

                                    this.winterVillage.antiFreezle.open_anti_inventory(player, anti_player);
                                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);

                                    return 1;
                                })
                );

        commands.register(this.winterVillage.getPluginMeta(), builder_antifreezle.build(), "Öffnet das Anti-Inventar für einen Spieler.", List.of());
    }

}
