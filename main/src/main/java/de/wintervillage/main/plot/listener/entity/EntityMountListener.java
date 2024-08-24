package de.wintervillage.main.plot.listener.entity;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityMountListener implements Listener {

    private final WinterVillage winterVillage;

    public EntityMountListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(EntityMountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Plot plot = this.winterVillage.plotHandler.byBounds(event.getMount().getLocation());
        if (plot == null) return;

        if (player.hasPermission("wintervillage.plot.bypass")) return;

        if (plot.owner().equals(player.getUniqueId()) || plot.members().contains(player.getUniqueId())) return;

        // cancel mounts in plots that the player is not a member of
        event.setCancelled(true);
        this.winterVillage.plotHandler.deny(player, event.getEntity().getLocation());
    }
}
