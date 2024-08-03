package de.wintervillage.main.death.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.economy.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

public class Listener_PlayerRespawn implements Listener {

    private WinterVillage winterVillage;

    public Listener_PlayerRespawn(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        Location location_death = this.winterVillage.deathManager.getDeathLocation(player);

        if(location_death != null){
            ItemStack item_compass = ItemUtils.createItemStack(Material.COMPASS, 1, MiniMessage.miniMessage().deserialize("<color:red><bold>Todesort:</bold> <color:white>x: <color:red>" + location_death.getBlockX()
                    + " <color:white>y: <color:red>" + location_death.getY() + " <color:white>z: <color:red>" + location_death.getBlockZ()));

            if(item_compass.getItemMeta() != null && item_compass.getItemMeta() instanceof CompassMeta){
                CompassMeta meta_compass = (CompassMeta) item_compass.getItemMeta();
                meta_compass.setLodestone(location_death);
                meta_compass.setLodestoneTracked(false);
                item_compass.setItemMeta(meta_compass);

                player.getInventory().addItem(item_compass);
                this.winterVillage.deathManager.removeDeathLocation(player);
            }
        }
    }

}
