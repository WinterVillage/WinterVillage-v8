package de.wintervillage.main.plot.listener.entity;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class HangingPlaceListener implements Listener {

    private final WinterVillage winterVillage;

    public HangingPlaceListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(HangingPlaceEvent event) {
        if (event.getPlayer() == null) return;

        Plot plot = this.winterVillage.plotHandler.byBounds(event.getBlock().getLocation());
        if (plot == null) return;

        if (event.getPlayer().hasPermission("wintervillage.plot.bypass")) return;

        if (plot.owner().equals(event.getPlayer().getUniqueId()) || plot.members().contains(event.getPlayer().getUniqueId())) return;

        // cancel hanging entities in plots that the player is not a member of
        event.setCancelled(true);
        this.winterVillage.plotHandler.deny(event.getPlayer(), event.getBlock().getLocation());
    }
}
