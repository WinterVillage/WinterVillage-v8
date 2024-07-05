package de.wintervillage.main.adventcalendar.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CMD_AdventsKalender {

    private WinterVillage winterVillage;

    public CMD_AdventsKalender(Commands commands){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.register(commands);
    }

    public void register(Commands commands){
        LiteralArgumentBuilder<CommandSourceStack> builder_adventskalender = Commands.literal("adventskalender")
                .executes(context -> {
                    if(!(context.getSource().getExecutor() instanceof Player player)){
                        context.getSource().getExecutor().sendMessage(this.winterVillage.PREFIX + "Dieser Command ist nur durch einen Spieler ausführbar.");
                        return 0;
                    }

                    this.winterVillage.adventCalendar.openAdventsCalender(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);

                    return 1;
                });

        commands.register(this.winterVillage.getPluginMeta(), builder_adventskalender.build(), "Öffnet den Adventskalender.", List.of("ac", "ak"));
    }

}
