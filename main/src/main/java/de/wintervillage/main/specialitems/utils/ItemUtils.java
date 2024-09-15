package de.wintervillage.main.specialitems.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

    public static ItemStack createItemStack(Material material, int amount, String display_name){
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta_item = item.getItemMeta();
        meta_item.displayName(Component.text(display_name));
        item.setItemMeta(meta_item);

        return item;
    }

    public static ItemStack createItemStack(Material material, int amount, Component display_name){
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta_item = item.getItemMeta();
        meta_item.displayName(display_name);
        item.setItemMeta(meta_item);

        return item;
    }
}
