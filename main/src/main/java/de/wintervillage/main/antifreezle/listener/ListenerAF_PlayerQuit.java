package de.wintervillage.main.antifreezle.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ListenerAF_PlayerQuit implements Listener {

    private WinterVillage winterVillage;

    public ListenerAF_PlayerQuit(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        this.winterVillage.antiFreezle.clear_anti_tools(player);
    }

}
