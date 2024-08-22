package de.wintervillage.main.plot.listener.block;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class BlockFromToListener implements Listener {

    private final WinterVillage winterVillage;

    public BlockFromToListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(BlockFromToEvent event) {
        Optional<Plot> fromPlot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(event.getBlock().getLocation()));
        Optional<Plot> toPlot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(event.getToBlock().getLocation()));

        if (fromPlot.isPresent() && toPlot.isPresent()) {
            if (!fromPlot.get().equals(toPlot.get())) {
                // either liquid or a dragon egg is trying to reach its destination plot
                event.setCancelled(true);
                return;
            }
        } else if (fromPlot.isPresent() && toPlot.isEmpty()) {
            // either liquid or a dragon egg is trying to leave its plot
            event.setCancelled(true);
            return;
        } else if (fromPlot.isEmpty() && toPlot.isPresent()) {
            // either liquid or a dragon egg is trying to enter a plot
            event.setCancelled(true);
        }
    }
}
