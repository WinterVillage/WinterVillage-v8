package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SpecialItem_FastFurnace extends SpecialItem {

    public SpecialItem_FastFurnace(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Fast Furnace"), Material.FURNACE, 1, true);
        this.setItem(item);
        this.setNameStr("fast_furnace");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        // Cancelled
        // | If the player tries to break a specialitem within a plot where he is not allowed to
        if (event.isCancelled()) return;

        World world = event.getBlock().getWorld();

        if(this.winterVillage.specialItems.isSIBlock(event.getBlock(), "fast_furnace")){
            event.setDropItems(false);
            world.dropItemNaturally(event.getBlock().getLocation(), this.getItem());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Cancelled
        // | If the player tries to place a specialitem within a plot where he is not allowed to
        if (event.isCancelled()) return;

        ItemStack item_placed = event.getItemInHand();

        if(isSpecialitem(item_placed)){
            this.winterVillage.specialItems.setSIBlock(event.getBlock(), "fast_furnace", true);
        }
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        Block block_furnace = event.getBlock();

        if(this.winterVillage.specialItems.isSIBlock(block_furnace, "fast_furnace")) {
            event.setBurnTime(event.getBurnTime() * 2);
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        Block block_furnace = event.getBlock();
        ItemStack item_outcome = event.getResult();

        if(this.winterVillage.specialItems.isSIBlock(block_furnace, "fast_furnace")){
            Random rdm = new Random();
            int amount_outcome = rdm.nextInt(1, 4);

            item_outcome.setAmount(amount_outcome);
            event.setResult(item_outcome);
        }
    }

    @EventHandler
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent event) {
        Block block_furnace = event.getBlock();

        if(this.winterVillage.specialItems.isSIBlock(block_furnace, "fast_furnace")) {
            event.setTotalCookTime(event.getTotalCookTime()/2);
        }
    }
}
