package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.event.events.PlayerUpdateEvent;
import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SpecialItem_WVETable extends SpecialItem {

    public SpecialItem_WVETable(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("WV Enchantment Table"), Material.ENCHANTING_TABLE, 1, true);
        this.setItem(item);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        super.onBlockBreak(event);

        World world = event.getBlock().getWorld();

        if(this.winterVillage.specialItems.isSIBlock(event.getBlock(), "wve_table")){
            event.setDropItems(false);
            world.dropItemNaturally(event.getBlock().getLocation(), this.getItem());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        super.onBlockPlace(event);

        ItemStack item_placed = event.getItemInHand();

        if(isSpecialitem(item_placed)){
            this.winterVillage.specialItems.setSIBlock(event.getBlock(), "wve_table", true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        if(event.getView().title().equals(Component.text("WV Enchantment Table", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))) {
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

        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);

        if(!(event.getPlayer() instanceof Player player))
            return;

        if(event.getView().title().equals(Component.text("WV Enchantment Table", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){
            ItemStack item_in_slot = event.getInventory().getItem(10);

            if(item_in_slot != null && item_in_slot.getType() != Material.AIR){
                player.getInventory().addItem(item_in_slot);
            }
        }
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        super.onPlayerUpdate(event);

        Player player = event.getPlayer();

        if(player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null
                && player.getOpenInventory().title().equals(Component.text("WV Enchantment Table", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){

            Inventory inventory = player.getOpenInventory().getTopInventory();
            ItemStack item = inventory.getItem(10);
            ItemStack item_enchantment = inventory.getItem(12);

            if(item != null && item_enchantment != null && EnchantmentUtils.isWVEnchantment(item_enchantment)
                    && item.getAmount() == 1 && EnchantmentUtils.getWVEnchantmentAmount(item) < 4){

                if(inventory.getItem(16) == null || inventory.getItem(16).getType() == Material.AIR){
                    ItemStack item_result = EnchantmentUtils.enchantWV(item, item_enchantment);
                    inventory.setItem(16, item_result);
                }

            } else if(inventory.getItem(16) != null && inventory.getItem(16).getType() != Material.AIR){
                inventory.setItem(16, new ItemStack(Material.AIR));
            }

        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        super.onPlayerInteract(event);

        Player player = event.getPlayer();

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();

            if(this.winterVillage.specialItems.isSIBlock(block, "wve_table")){
                EnchantmentUtils.openWVEnchantmentTable(player);
                event.setCancelled(true);
            }
        }
    }
}
