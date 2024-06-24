package de.wintervillage.main.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class Listener_PlayerMove implements Listener {

    private final WinterVillage winterVillage;

    public Listener_PlayerMove(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.register();
    }

    private void register() {
        Bukkit.getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if(this.winterVillage.freeze_all || this.winterVillage.frozen_players.contains(event.getPlayer())){
            event.setCancelled(true);
        }
    }
}
