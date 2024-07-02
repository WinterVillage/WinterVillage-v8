package de.wintervillage.main.specialitems.listener;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ListenerSI_InventoryClickClose implements Listener {

    private WinterVillage winterVillage;

    public ListenerSI_InventoryClickClose(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getView().title().equals(Component.text("Entzauberungstisch", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){
            if(event.getWhoClicked() instanceof Player player){
                if(event.getCurrentItem()!=null && event.getCurrentItem().getType().equals(Material.BLACK_STAINED_GLASS_PANE)){
                    event.setCancelled(true);
                    return;
                }

                Inventory inventory = event.getInventory();
                ItemStack item = inventory.getItem(13);

                if(item == null)
                    return;

                int enchantment_amount = item.getEnchantments().size();
                int inventory_size = 45 + (enchantment_amount - 1)/4 * 9;

                if(event.getSlot() >= inventory_size) {
                    event.setCancelled(false);
                    return;
                }

                if(event.getSlot() != 13 && event.getCurrentItem()!=null && event.getCurrentItem().getType().equals(Material.ENCHANTED_BOOK)) {
                    ItemStack item_enchantment = event.getCurrentItem();
                    EnchantmentStorageMeta meta_enchantment = (EnchantmentStorageMeta) item_enchantment.getItemMeta();

                    ItemMeta meta_item = item.getItemMeta();

                    ItemStack item_space = this.winterVillage.itemUtils.createItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, "<color:black>");
                    inventory.setItem(event.getSlot(), item_space);

                    player.getInventory().addItem(item_enchantment);

                    //Entzauberung
                    if(!meta_enchantment.getStoredEnchants().keySet().isEmpty()){
                        Enchantment enchantment = meta_enchantment.getStoredEnchants().keySet().toArray(new Enchantment[0])[0];
                        meta_item.removeEnchant(enchantment);
                        item.setItemMeta(meta_item);
                        inventory.setItem(13, item);
                    }

                    event.setCancelled(true);
                } else if(event.getSlot() == 13 && event.getCurrentItem()!=null && !event.getCurrentItem().getType().equals(Material.AIR)){
                    event.setCancelled(true);
                    inventory.setItem(13, new ItemStack(Material.AIR));
                    player.getInventory().addItem(item);
                    player.closeInventory();
                }

            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(event.getView().title().equals(Component.text("Entzauberungstisch", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){
            ItemStack item = event.getInventory().getItem(13);
            if(item == null || item.getType().equals(Material.AIR))
                return;

            event.getPlayer().getInventory().addItem(item);
        }
    }

}
