package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class SpecialItem_TimberAxt extends SpecialItem {

    public SpecialItem_TimberAxt() {
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("TimberAxt"), Material.IRON_AXE, 1, true);
        this.setItem(item);
        this.setNameStr("timberaxt");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        // Cancelled
        // | If the player tries to timber a tree within a plot where he is not allowed to
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ItemStack item_in_hand = player.getInventory().getItemInMainHand();

        if(isSpecialitem(item_in_hand)){
            if(event.getBlock().getType().name().toLowerCase().contains("log")){
                timber(event.getBlock().getLocation());
                event.setCancelled(true);
            }
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
