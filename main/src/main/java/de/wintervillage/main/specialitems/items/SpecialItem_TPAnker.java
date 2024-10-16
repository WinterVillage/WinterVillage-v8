package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class SpecialItem_TPAnker extends SpecialItem {

    public SpecialItem_TPAnker(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("TP Anker"), Material.ANVIL, 1, true);
        this.setItem(item);
        this.setNameStr("tp_anker");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(isSpecialitem(event.getItemInHand())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();

        if(isSpecialitem(event.getItem()) && event.getAction() == Action.RIGHT_CLICK_BLOCK){
            // Location abspeichern
            player.sendMessage(this.winterVillage.PREFIX + "§fDu hast deinen TP Anker Punkt gesetzt!");
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        Location location = event.getRespawnLocation(); // Korrekte Location laden
        event.setRespawnLocation(location);
    }

}
