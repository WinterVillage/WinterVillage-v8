package de.wintervillage.main.death;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.death.listener.Listener_PlayerDeath;
import de.wintervillage.main.death.listener.Listener_PlayerRespawn;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class DeathManager {

    private WinterVillage winterVillage;

    private HashMap<Player, Location> death_locations;

    @Inject
    public DeathManager(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.death_locations = new HashMap<>();

        new Listener_PlayerDeath(this.winterVillage);
        new Listener_PlayerRespawn(this.winterVillage);
    }

    public void setDeathLocation(Player player, Location location){
        this.death_locations.put(player, location);
    }

    public void removeDeathLocation(Player player){
        this.death_locations.remove(player);
    }

    public Location getDeathLocation(Player player){
        return this.death_locations.get(player);
    }

}
