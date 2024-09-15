package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.utils.ItemUtils;
import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class SpecialItem_DisenchantmentTable extends SpecialItem {

    public SpecialItem_DisenchantmentTable(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Disenchantment Table"), Material.GRINDSTONE, 1, true);
        this.setItem(item);
        this.setNameStr("disenchantment_table");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Cancelled
        // | If the player tries to place a specialitem within a plot where he is not allowed to
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ItemStack item_placed = event.getItemInHand();

        if(isSpecialitem(item_placed)){
            this.winterVillage.specialItems.setSIBlock(event.getBlock(), "disenchantment_table", true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        World world = event.getBlock().getWorld();

        if(this.winterVillage.specialItems.isSIBlock(event.getBlock(), "disenchantment_table")){
            event.setDropItems(false);
            world.dropItemNaturally(event.getBlock().getLocation(), this.getItem());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
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
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getView().title().equals(Component.text("Entzauberungstisch", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){

            ItemStack item = event.getInventory().getItem(13);
            if(item == null || item.getType().equals(Material.AIR))
                return;

            event.getPlayer().getInventory().addItem(item);

        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();

            if(this.winterVillage.specialItems.isSIBlock(block, "disenchantment_table")){
                if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
                    EnchantmentUtils.openDisenchantmentTable(player, player.getInventory().getItemInMainHand());
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
                event.setCancelled(true);
            }
        }
    }
}
