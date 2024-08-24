package de.wintervillage.main.plot.listener.entity;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class EntityExplodeListener implements Listener {

    private final WinterVillage winterVillage;

    public EntityExplodeListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(EntityExplodeEvent event) {
        Optional<Plot> optionalPlot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(event.getLocation()));
        if (optionalPlot.isEmpty()) {
            event.blockList().removeIf(block -> this.winterVillage.plotHandler.byBounds(block.getLocation()) != null);
            return;
        }

        // explosion in inner plot
        Plot plot = optionalPlot.get();

        // remove blocks that are not in the outer plot
        event.blockList().removeIf(block -> {
            Plot outside = this.winterVillage.plotHandler.byBounds(block.getLocation());
            return outside != null && !plot.uniqueId().equals(outside.uniqueId());
        });
    }
}
