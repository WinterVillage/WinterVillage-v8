package de.wintervillage.main.specialitems;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.event.events.PlayerUpdateEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SpecialItem implements Listener {

    public WinterVillage winterVillage;
    private ItemStack item;

    public SpecialItem(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public Component getName(){
        if(item == null || item.getItemMeta() == null || item.getItemMeta().lore() == null || item.getItemMeta().lore().isEmpty())
            return Component.text("");

        return item.getItemMeta().lore().getFirst();
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean isSpecialitem(ItemStack item){
        return item != null && item.getItemMeta() != null
                && item.getItemMeta().lore() != null && !item.getItemMeta().lore().isEmpty()
                && item.getItemMeta().lore().get(0).equals(this.getName());
    }

    public void onBlockPlace(BlockPlaceEvent event) {};
    public void onBlockBreak(BlockBreakEvent event) {};

    public void onFurnaceBurn(FurnaceBurnEvent event) {};
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent event) {};
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {};

    public void onInventoryClick(InventoryClickEvent event) {};
    public void onInventoryClose(InventoryCloseEvent event) {};

    public void onPlayerInteract(PlayerInteractEvent event) {};

    public void onPlayerUpdate(PlayerUpdateEvent event) {};

}
