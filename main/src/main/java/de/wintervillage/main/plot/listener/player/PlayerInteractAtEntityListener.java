package de.wintervillage.main.plot.listener.player;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerInteractAtEntityListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerInteractAtEntityListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void execute(PlayerInteractAtEntityEvent event) {
        if (event instanceof PlayerInteractEntityEvent) return;

        Player player = event.getPlayer();

        Plot plot = this.winterVillage.plotHandler.byBounds(event.getRightClicked().getLocation());
        if (plot == null) return;

        if (player.hasPermission("wintervillage.plot.bypass")) return;

        if (plot.owner().equals(player.getUniqueId()) || plot.members().contains(player.getUniqueId())) return;

        // cancel interaction with entities in plots that the player is not a member of
        event.setCancelled(true);
        this.winterVillage.plotHandler.deny(player, event.getRightClicked().getLocation());
    }
}
