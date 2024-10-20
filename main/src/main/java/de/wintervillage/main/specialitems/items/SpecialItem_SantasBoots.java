package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class SpecialItem_SantasBoots extends SpecialItem {

    public SpecialItem_SantasBoots(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Santa's Boots"), Material.DIAMOND_BOOTS, 1, true);
        this.setItem(item);
        this.setNameStr("santas_boots");
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof Player player){
            if(isSpecialitem(player.getInventory().getBoots())) {
                if(event.getCause() == EntityDamageEvent.DamageCause.FALL)
                    event.setCancelled(true);
            }
        }
    }

}
