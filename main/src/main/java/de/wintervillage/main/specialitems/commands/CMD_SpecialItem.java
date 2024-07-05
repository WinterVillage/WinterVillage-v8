package de.wintervillage.main.specialitems.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
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
                                    ItemStack item = new ItemStack(Material.AIR);

                                    if(item_name.equalsIgnoreCase("timberaxt")){
                                        item = this.winterVillage.specialItems.getSpecialItem(Component.text("TimberAxt"), Material.IRON_AXE, 1, true);
                                    } else if(item_name.equalsIgnoreCase("disenchantmenttable")){
                                        item = this.winterVillage.specialItems.getSpecialItem(Component.text("Disenchantment Table"), Material.GRINDSTONE, 1, true);
                                    } else if(item_name.equalsIgnoreCase("fastfurnace")){
                                        item = this.winterVillage.specialItems.getSpecialItem(Component.text("Fast Furnace"), Material.FURNACE, 1, true);
                                    } else if(item_name.equalsIgnoreCase("backpack")){
                                        item = this.winterVillage.specialItems.getSpecialItem(Component.text("Backpack"), Material.SHULKER_BOX, 1, true);
                                    } else if(item_name.equalsIgnoreCase("santaspants")){
                                        item = this.winterVillage.specialItems.getSpecialItem(Component.text("Santa's Pants"), Material.DIAMOND_LEGGINGS, 1, true);
                                    } else if(item_name.equalsIgnoreCase("wvtable")){
                                        item = this.winterVillage.specialItems.getSpecialItem(Component.text("WV Enchantment Table"), Material.ENCHANTING_TABLE, 1, true);
                                    } else if(item_name.equalsIgnoreCase("wve_autosmelt")) {
                                        item = this.winterVillage.specialItems.getSpecialItem(Component.text("WVE: AutoSmelt"), Material.ENCHANTED_BOOK, 1, true);
                                    }

                                    player.getInventory().addItem(item);

                                    return 1;
                                })
                );
        commands.register(this.winterVillage.getPluginMeta(), builder_specialitem.build(), "Gibt dir ein SpecialItem.", List.of());
    }

}
