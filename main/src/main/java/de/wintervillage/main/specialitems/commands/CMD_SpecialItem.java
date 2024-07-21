package de.wintervillage.main.specialitems.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.specialitems.SpecialItem;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CMD_SpecialItem {

    private WinterVillage winterVillage;

    public CMD_SpecialItem(Commands commands){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.register(commands);
    }

    public void register(Commands commands){
        LiteralArgumentBuilder<CommandSourceStack> builder_specialitem = Commands.literal("specialitem")
                .then(
                        Commands.argument("item_name", StringArgumentType.string())
                                .executes((source) -> {
                                    if(!(source.getSource().getExecutor() instanceof Player player)){
                                        source.getSource().getSender().sendMessage(this.winterVillage.PREFIX + "Dieser Command ist nur durch einen Spieler ausführbar.");
                                        return 0;
                                    }

                                    String item_name = source.getArgument("item_name", String.class);
                                    ItemStack item;

                                    SpecialItem specialItem = this.winterVillage.specialItems.getSIByName(item_name);

                                    if(specialItem != null){
                                        item = specialItem.getItem();
                                        player.getInventory().addItem(item);
                                    } else {
                                        player.sendMessage(this.winterVillage.PREFIX + "Das SpecialItem " + item_name + " existiert nicht.");
                                    }

                                    return 1;
                                })
                );
        commands.register(this.winterVillage.getPluginMeta(), builder_specialitem.build(), "Gibt dir ein SpecialItem.", List.of());
    }

}
