package de.wintervillage.main.plot.listener.block;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class StructureGrowListener implements Listener {

    private final WinterVillage winterVillage;

    public StructureGrowListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(StructureGrowEvent event) {
        Location origin = event.getLocation();
        Optional<Plot> originPlot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(origin));

        event.getBlocks().removeIf(blockState -> {
            Optional<Plot> blockPlot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(blockState.getLocation()));

            if (originPlot.isPresent()) {
                // block inside another plot -> removing
                // block inside the same plot -> keeping
                return blockPlot.isPresent() && !originPlot.get().equals(blockPlot.get());
            } else {
                // block is inside a plot -> removing
                // block is outside any plot -> keeping
                return blockPlot.isPresent();
            }
        });
    }
}
