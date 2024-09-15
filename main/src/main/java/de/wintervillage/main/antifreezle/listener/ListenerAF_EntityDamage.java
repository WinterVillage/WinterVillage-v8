package de.wintervillage.main.antifreezle.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class ListenerAF_EntityDamage implements Listener {

    private WinterVillage winterVillage;

    public ListenerAF_EntityDamage(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(!(event.getEntity() instanceof Player player))
            return;

        if(event.getCause() == EntityDamageEvent.DamageCause.FALL){
            if(this.winterVillage.antiFreezle.is_anti_tool_activated(player, "Rocket-Boost")){
                event.setCancelled(true);
                this.winterVillage.antiFreezle.deactivate_anti_tool(player, "Rocket-Boost");
            }
        }
    }

}
