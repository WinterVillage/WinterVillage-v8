package de.wintervillage.main.specialitems.utils;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class EnchantmentUtils {

    private WinterVillage winterVillage;

    public EnchantmentUtils(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public void openDisenchantmentTable(Player player, ItemStack item){
        int enchantment_amount = item.getEnchantments().size();
        int inventory_size = 45 + (enchantment_amount - 1)/4 * 9;

        Inventory inventory = Bukkit.createInventory(null, inventory_size,
                Component.text("Entzauberungstisch", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));

        ItemStack item_space = this.winterVillage.itemUtils.createItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, "§0");

        for(int i = 0; i < inventory_size; i++){
            inventory.setItem(i, item_space);
        }

        int slot_item = 13;
        inventory.setItem(slot_item, item);

        Enchantment[] enchantments = item.getEnchantments().keySet().toArray(new Enchantment[0]);
        Integer[] levels = item.getEnchantments().values().toArray(new Integer[0]);

        for(int i = 0; i < enchantment_amount; i++){
            int slot_enchantment = 28 + 2*i + i/4;

            ItemStack enchanted_book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta_enchanted_book = (EnchantmentStorageMeta) enchanted_book.getItemMeta();
            meta_enchanted_book.addStoredEnchant(enchantments[i], levels[i], true);
            enchanted_book.setItemMeta(meta_enchanted_book);

            inventory.setItem(slot_enchantment, enchanted_book);
        }

        player.openInventory(inventory);
    }

}
