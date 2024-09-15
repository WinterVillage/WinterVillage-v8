package de.wintervillage.main.specialitems.utils;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class EnchantmentUtils {

    private final WinterVillage winterVillage;

    public EnchantmentUtils(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public static void openDisenchantmentTable(Player player, ItemStack item){
        int enchantment_amount = item.getEnchantments().size();
        int inventory_size = 45 + (enchantment_amount - 1)/4 * 9;

        Inventory inventory = Bukkit.createInventory(null, inventory_size,
                Component.text("Entzauberungstisch", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));

        ItemStack item_space = ItemUtils.createItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, "§0");

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

    public static void openWVEnchantmentTable(Player player){
        Inventory inventory = Bukkit.createInventory(null, 27, Component.text("WV Enchantment Table", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));

        ItemStack item_space = ItemUtils.createItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, "§0");

        for(int i = 0; i < 27; i++){
            if(i == 10 || i == 12 || i == 16)
                continue;

            inventory.setItem(i, item_space);
        }

        player.openInventory(inventory);
    }

    public static boolean isWVEnchantment(ItemStack item){
        ItemMeta meta_item = item.getItemMeta();

        if(meta_item == null || item.getType() != Material.ENCHANTED_BOOK || !meta_item.hasLore())
            return false;

        if(!PlainTextComponentSerializer.plainText().serialize(meta_item.lore().getFirst()).toLowerCase().contains("wve"))
            return false;

        return true;
    }

    /**
     * Gibt das WVEnchantment eines WV Enchantment Buches zurück
     * **/
    public static Component getWVEnchantment(ItemStack item){
        ItemMeta meta_item = item.getItemMeta();

        if(meta_item == null || item.getType() != Material.ENCHANTED_BOOK || !meta_item.hasLore())
            return null;

        return meta_item.lore().getFirst();
    }

    public static boolean hasWVEnchantment(ItemStack item, String enchantment){
        ItemMeta meta_item = item.getItemMeta();

        if(meta_item == null || !meta_item.hasLore())
            return false;

        for(Component component : meta_item.lore()){
            String string_component = PlainTextComponentSerializer.plainText().serialize(component).toLowerCase();
            if(string_component.contains(enchantment.toLowerCase()) && string_component.contains("wve"))
                return true;
        }

        return false;
    }

    public static ItemStack enchantWV(ItemStack item, ItemStack item_enchantment){
        ItemStack item_enchanted = item.clone();
        ItemMeta meta_item = item_enchanted.getItemMeta();

        Component component_enchantment = getWVEnchantment(item_enchantment);

        if(component_enchantment == null)
            return item_enchanted;

        int index_lore = getWVEnchantmentAmount(item_enchanted);

        if(index_lore >= 4)
            return item_enchanted;

        String enchantment_string = PlainTextComponentSerializer.plainText().serialize(component_enchantment).split(" - ")[1];
        Component component_enchantment_string = Component.text(enchantment_string, NamedTextColor.RED).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false);

        if(index_lore == 0){
            meta_item.lore(List.of(component_enchantment_string));
        } else {
            meta_item.lore().set(index_lore, component_enchantment_string);
        }

        item_enchanted.setItemMeta(meta_item);

        return item_enchanted;
    }

    public static int getWVEnchantmentAmount(ItemStack item){
        int amount = 0;
        ItemMeta meta_item = item.getItemMeta();

        if(!item.hasItemMeta() || !item.getItemMeta().hasLore())
            return amount;

        for(Component component : meta_item.lore()){
            if(!PlainTextComponentSerializer.plainText().serialize(component).isEmpty())
                amount++;
        }

        return amount;
    }

    public static ItemStack decreaseRepairCost(ItemStack item, int amount) {
        /*net.minecraft.server.v1_16_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tagCompound = nmsStack.getOrCreateTag();

        if (tagCompound.hasKey("RepairCost")) {
            int repairCost = tagCompound.getInt("RepairCost");
            repairCost -= amount;
            if (repairCost < 0) {
                repairCost = 0;
            }
            tagCompound.setInt("RepairCost", repairCost);
            nmsStack.setTag(tagCompound);
        }

        return CraftItemStack.asBukkitCopy(nmsStack);*/
        return null;
    }

}
