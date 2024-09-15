package de.wintervillage.main.specialitems.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CMD_Disenchant {

    private WinterVillage winterVillage;

    public CMD_Disenchant(Commands commands){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.register(commands);
    }

    public void register(Commands commands){
        final LiteralArgumentBuilder<CommandSourceStack> builder_disenchant = Commands.literal("disenchant")
                .requires((source) -> source.getSender().hasPermission("wintervillage.command.disenchant"))
                .executes((source) -> {
                    if(!(source.getSource().getExecutor() instanceof Player player)){
                        source.getSource().getSender().sendMessage(this.winterVillage.PREFIX + "Dieser Command ist nur durch einen Spieler ausführbar.");
                        return 0;
                    }

                    if(!player.isOp()){
                        player.sendMessage(this.winterVillage.PREFIX + "Du hast keine Berechtigung für diesen Command.");
                        return 0;
                    }

                    EnchantmentUtils.openDisenchantmentTable(player, player.getInventory().getItemInMainHand());
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    return 1;
                });
        commands.register(this.winterVillage.getPluginMeta(), builder_disenchant.build(), "Öffnet den Disenchantment-Table.", List.of());
    }

}
