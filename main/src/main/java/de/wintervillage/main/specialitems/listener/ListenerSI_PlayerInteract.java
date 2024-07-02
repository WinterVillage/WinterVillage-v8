package de.wintervillage.main.specialitems.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ListenerSI_PlayerInteract implements Listener {

    private WinterVillage winterVillage;

    public ListenerSI_PlayerInteract(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null){
            Block block = event.getClickedBlock();

            if(this.winterVillage.specialItems.isSIBlock(block, "disenchantment_table")){
                if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
                    this.winterVillage.enchantmentUtils.openDisenchantmentTable(player, player.getInventory().getItemInMainHand());
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
                event.setCancelled(true);
            }
        }
    }

}
