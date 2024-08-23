package de.wintervillage.main.plot.listener.entity;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class VehicleDestroyListener implements Listener {

    private final WinterVillage winterVillage;

    public VehicleDestroyListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(VehicleDestroyEvent event) {
        if (!(event.getAttacker() instanceof Player player)) return;

        Plot plot = this.winterVillage.plotHandler.byBounds(event.getVehicle().getLocation());
        if (plot == null) return;

        if (player.hasPermission("wintervillage.plot.bypass")) return;

        if (plot.owner().equals(player.getUniqueId()) || plot.members().contains(player.getUniqueId())) return;

        // cancel destroying of vehicles in plots that the player is not a member of
        event.setCancelled(true);
        player.sendMessage(Component.text("You are not allowed to destroy this vehicle", NamedTextColor.RED));
    }
}
