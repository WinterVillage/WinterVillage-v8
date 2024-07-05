package de.wintervillage.main.specialitems.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.economy.utils.ItemUtils;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
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

                    ItemStack item_space = ItemUtils.createItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, "<color:black>");
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
                    player.updateInventory();
                }

            }
        } else if(event.getView().title().equals(Component.text("Backpack", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){

            if(this.winterVillage.specialItems.isSpecialItem(event.getCurrentItem(), Component.text("Backpack")))
                event.setCancelled(true);

            event.setCancelled(event.getClick() == ClickType.NUMBER_KEY && event.getRawSlot() > 26);

        } else if(event.getView().title().equals(Component.text("WV Enchantment Table", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))) {
            Inventory inventory = event.getInventory();
            ItemStack item_clicked = event.getCurrentItem();

            if(item_clicked != null && item_clicked.getType() == Material.BLACK_STAINED_GLASS_PANE){
                event.setCancelled(true);
                return;
            }

            ItemStack item_in_slot = inventory.getItem(10);
            ItemStack item_wv_enchantment = inventory.getItem(12);

            ItemStack item_result = inventory.getItem(16);

            if(event.getSlot() == 16 && item_result != null && item_result.getType() != Material.AIR){
                inventory.setItem(10, new ItemStack(Material.AIR));
                inventory.setItem(12, new ItemStack(Material.AIR));
            } else if(event.getSlot() == 16){
                event.setCancelled(true);
            }

            if(item_in_slot == null || item_in_slot.getType() == Material.AIR
            || item_wv_enchantment == null || item_wv_enchantment.getType() == Material.AIR){
                if(item_result != null && item_result.getType() != Material.AIR){
                    inventory.setItem(16, new ItemStack(Material.AIR));
                }
            }

            /*ItemStack item_result = inventory.getItem(16);

            if(event.getSlot() == 16 && (item_result == null || item_result.getType() == Material.AIR)){
                event.setCancelled(true);
                return;
            } else if(event.getSlot() == 16){
                inventory.setItem(10, new ItemStack(Material.AIR));
                inventory.setItem(12, new ItemStack(Material.AIR));
            }

            if(item_in_slot != null && item_in_slot.getType() != Material.AIR){
                if(item_wv_enchantment != null && EnchantmentUtils.isWVEnchantment(item_wv_enchantment)){
                    if(EnchantmentUtils.getWVEnchantmentAmount(item_in_slot) >= 4)
                        return;

                    inventory.setItem(16, EnchantmentUtils.enchantWV(item_in_slot, item_wv_enchantment));
                }
            }

            if(item_in_slot == null || item_in_slot.getType() == Material.AIR
                || item_wv_enchantment == null || item_wv_enchantment.getType() == Material.AIR){

                if(inventory.getItem(16) != null && inventory.getItem(16).getType() != Material.AIR){
                    inventory.setItem(16, new ItemStack(Material.AIR));
                }

            }*/

        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(!(event.getPlayer() instanceof Player player))
            return;

        if(event.getView().title().equals(Component.text("Entzauberungstisch", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){

            ItemStack item = event.getInventory().getItem(13);
            if(item == null || item.getType().equals(Material.AIR))
                return;

            event.getPlayer().getInventory().addItem(item);

        } else if(event.getView().title().equals(Component.text("Backpack", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){
            ItemStack item_backpack = player.getInventory().getItemInMainHand();

            if(this.winterVillage.specialItems.isSpecialItem(item_backpack, Component.text("Backpack"))){
                BlockStateMeta blockStateMeta = (BlockStateMeta) item_backpack.getItemMeta();
                ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                shulkerBox.getInventory().setContents(event.getInventory().getContents());
                blockStateMeta.setBlockState(shulkerBox);
                item_backpack.setItemMeta(blockStateMeta);

                //TODO: Save Inventory in Database
            }
        } else if(event.getView().title().equals(Component.text("WV Enchantment Table", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){
            ItemStack item_in_slot = event.getInventory().getItem(10);

            if(item_in_slot != null && item_in_slot.getType() != Material.AIR){
                player.getInventory().addItem(item_in_slot);
            }
        }
    }

}
