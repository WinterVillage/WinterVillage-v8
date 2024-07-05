package de.wintervillage.main.specialitems.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ListenerSI_Furnace implements Listener {

    private WinterVillage winterVillage;

    public ListenerSI_Furnace(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event){
        Block block_furnace = event.getBlock();

        if(this.winterVillage.specialItems.isSIBlock(block_furnace, "fast_furnace")) {
            event.setBurnTime(event.getBurnTime() * 2);
        }
    }

    @EventHandler
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent event){
        Block block_furnace = event.getBlock();

        if(this.winterVillage.specialItems.isSIBlock(block_furnace, "fast_furnace")) {
            event.setTotalCookTime(event.getTotalCookTime()/2);
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event){
        Block block_furnace = event.getBlock();
        ItemStack item_outcome = event.getResult();

        if(this.winterVillage.specialItems.isSIBlock(block_furnace, "fast_furnace")){
            Random rdm = new Random();
            int amount_outcome = rdm.nextInt(1, 4);

            item_outcome.setAmount(amount_outcome);
            event.setResult(item_outcome);
        }
    }

}
