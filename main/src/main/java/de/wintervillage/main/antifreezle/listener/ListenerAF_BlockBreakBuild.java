package de.wintervillage.main.antifreezle.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ListenerAF_BlockBreakBuild implements Listener {

    private final WinterVillage winterVillage;

    public ListenerAF_BlockBreakBuild(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        //Tool-Break
        if(this.winterVillage.antiFreezle.is_anti_tool_activated(player, "tool-break")){
            event.setCancelled(true);

            player.getWorld().dropItem(player.getLocation(), player.getInventory().getItemInMainHand());
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        }

        if(this.winterVillage.antiFreezle.is_anti_tool_activated(player, "tool-swap")){
            int slot_swap = 0;
            ItemStack item_swap = player.getInventory().getItem(slot_swap);

            for(int i = 0; i < 9; i++){
                ItemStack item = player.getInventory().getItem(i);

                if(item == null || item.getType().equals(Material.AIR))
                    continue;

                String str_item_type = item.getType().name().toLowerCase();
                if(str_item_type.contains("pickaxe") || str_item_type.contains("shovel") || str_item_type.contains("axe")){
                    slot_swap = i;
                    item_swap = item;
                    break;
                }
            }

            player.getInventory().setItem(slot_swap, player.getInventory().getItemInMainHand());
            player.getInventory().setItemInMainHand(item_swap);
        }
    }

}
