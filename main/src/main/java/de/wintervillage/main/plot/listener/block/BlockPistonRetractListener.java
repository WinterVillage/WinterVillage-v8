package de.wintervillage.main.plot.listener.block;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Optional;

public class BlockPistonRetractListener implements Listener {

    private final WinterVillage winterVillage;

    public BlockPistonRetractListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(BlockPistonRetractEvent event) {
        BlockFace direction = event.getDirection().getOppositeFace();
        Vector vector = new Vector(direction.getModX(), direction.getModY(), direction.getModZ());

        Optional<Plot> pistonPlot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(event.getBlock().getLocation()));

        event.getBlocks().forEach(block -> {
            Location moved = block.getLocation().add(vector);
            Optional<Plot> targetPlot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(moved));

            if (pistonPlot.isPresent() && targetPlot.isPresent()) {
                // piston inside a plot tries to pull a block inside another plot
                if (!pistonPlot.get().equals(targetPlot.get())) {
                    event.setCancelled(true);
                    return;
                }
            } else if (pistonPlot.isEmpty() && targetPlot.isPresent()) {
                // piston without a plot tries to pull a block inside a plot
                event.setCancelled(true);
                return;
            } else if (pistonPlot.isPresent() && targetPlot.isEmpty()) {
                // piston inside a plot tries to pull a block outside a plot
                // do nothing
                return;
            }
        });
    }
}
