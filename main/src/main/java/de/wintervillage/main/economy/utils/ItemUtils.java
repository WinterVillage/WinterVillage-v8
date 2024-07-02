package de.wintervillage.main.economy.utils;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemUtils {

    private WinterVillage winterVillage;

    public ItemUtils(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public ItemStack createItemStack(Material material, int amount, String display_name){
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta_item = item.getItemMeta();
        meta_item.displayName(Component.text(display_name));
        item.setItemMeta(meta_item);

        return item;
    }

}
