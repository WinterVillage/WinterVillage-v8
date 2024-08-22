package de.wintervillage.main.plot.listener.misc;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class InventoryMoveItemListener implements Listener {

    private final WinterVillage winterVillage;

    public InventoryMoveItemListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(InventoryMoveItemEvent event) {
        Inventory source = event.getSource();
        Inventory destination = event.getDestination();

        Optional<Plot> sourcePlot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(source.getLocation()));
        Optional<Plot> destinationPlot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(destination.getLocation()));

        if (sourcePlot.isPresent() && destinationPlot.isPresent()) {
            if (!sourcePlot.get().equals(destinationPlot.get())) {
                event.setCancelled(true);
                return;
            }
        } else if (sourcePlot.isEmpty() && destinationPlot.isPresent()) {
            event.setCancelled(true);
            return;
        } else if (sourcePlot.isPresent() && destinationPlot.isEmpty()) {
            return;
        }
    }
}
