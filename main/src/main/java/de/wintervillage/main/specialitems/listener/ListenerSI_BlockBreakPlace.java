package de.wintervillage.main.specialitems.listener;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class ListenerSI_BlockBreakPlace implements Listener {

    private WinterVillage winterVillage;

    public ListenerSI_BlockBreakPlace(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        ItemStack item_in_hand = player.getInventory().getItemInMainHand();
        World world = event.getBlock().getWorld();

        if(this.winterVillage.specialItems.isSpecialItem(item_in_hand, Component.text("TimberAxt"))){
            if(event.getBlock().getType().name().toLowerCase().contains("log")){
                timber(event.getBlock().getLocation());
                event.setCancelled(true);
            }
        }

        if(this.winterVillage.specialItems.isSIBlock(event.getBlock(), "disenchantment_table")){
            event.setDropItems(false);
            ItemStack item_disenchantment_table = this.winterVillage.specialItems.getSpecialItem(Component.text("Disenchantment Table"), Material.ENCHANTING_TABLE, 1, true);
            world.dropItemNaturally(event.getBlock().getLocation(), item_disenchantment_table);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        ItemStack item_placed = event.getItemInHand();

        if(this.winterVillage.specialItems.isSpecialItem(item_placed, Component.text("Disenchantment Table"))){
            this.winterVillage.specialItems.setSIBlock(event.getBlock(), "disenchantment_table", true);
        }
    }

    private void timber(Location location){
        World world = location.getWorld();
        world.getBlockAt(location).breakNaturally();

        for(double alpha = 0; alpha < 2*Math.PI; alpha += Math.PI/4){
            Location location_around = location.clone().add(Math.cos(alpha), 0, Math.sin(alpha));
            if(location_around.getBlock().getType().name().toLowerCase().contains("log")){
                timber(location_around);
            }
        }

        Location location_above = location.clone().add(0, 1, 0);
        if(location_above.getBlock().getType().name().toLowerCase().contains("log")){
            timber(location_above);
        }
    }

}
