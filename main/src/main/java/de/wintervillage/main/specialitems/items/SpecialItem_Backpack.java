package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class SpecialItem_Backpack extends SpecialItem {

    public SpecialItem_Backpack(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Backpack"), Material.SHULKER_BOX, 1, true);
        this.setItem(item);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        super.onBlockPlace(event);

        if(isSpecialitem(event.getItemInHand()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        if(event.getView().title().equals(Component.text("Backpack", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){

            if(isSpecialitem(event.getCurrentItem()))
                event.setCancelled(true);

            event.setCancelled(event.getClick() == ClickType.NUMBER_KEY && event.getRawSlot() > 26);

        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);

        if(!(event.getPlayer() instanceof Player player))
            return;

        if(event.getView().title().equals(Component.text("Backpack", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){
            ItemStack item_backpack = player.getInventory().getItemInMainHand();

            if(isSpecialitem(item_backpack)){
                BlockStateMeta blockStateMeta = (BlockStateMeta) item_backpack.getItemMeta();
                ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                shulkerBox.getInventory().setContents(event.getInventory().getContents());
                blockStateMeta.setBlockState(shulkerBox);
                item_backpack.setItemMeta(blockStateMeta);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        super.onPlayerInteract(event);

        Player player = event.getPlayer();

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR){
            if(player.getInventory().getItemInMainHand().getType() != Material.AIR){

                if(isSpecialitem(player.getInventory().getItemInMainHand())){
                    BlockStateMeta blockStateMeta = (BlockStateMeta) player.getInventory().getItemInMainHand().getItemMeta();
                    ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();

                    Inventory inventory_backpack = Bukkit.createInventory(null, InventoryType.SHULKER_BOX, Component.text("Backpack", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
                    inventory_backpack.setContents(shulkerBox.getInventory().getContents());

                    player.openInventory(inventory_backpack);

                    event.setCancelled(true);
                }

            }
        }
    }
}
