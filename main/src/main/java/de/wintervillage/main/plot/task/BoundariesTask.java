package de.wintervillage.main.plot.task;

import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.WinterVillage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class BoundariesTask implements Runnable {

    private final WinterVillage winterVillage;

    private final Player player;
    private int taskId;

    private static final Map<Integer, BoundariesTask> TASKS = new HashMap<>();

    public BoundariesTask(Player player) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.player = player;
    }

    public int start() {
        this.taskId = Bukkit.getScheduler().runTaskTimer(this.winterVillage, this, 0L, 10L).getTaskId();
        TASKS.put(this.taskId, this);
        return this.taskId;
    }

    public void stop() {
        TASKS.remove(this.taskId);
        if (Bukkit.getScheduler().isCurrentlyRunning(this.taskId)) Bukkit.getScheduler().cancelTask(this.taskId);
    }

    public static BoundariesTask task(int taskId) {
        return TASKS.get(taskId);
    }

    /**
     * Shows the player the area of the plot they have selected
     */
    @Override
    public void run() {
        if (!this.player.getPersistentDataContainer().has(this.winterVillage.plotHandler.plotBoundariesKey)) return;
        this.winterVillage.plotHandler.getPlotCache().forEach(plot -> this.render(plot.boundingBox()));
    }

    private void render(BoundingBox2D boundingBox) {
        int minX = (int) boundingBox.getMinX();
        int minZ = (int) boundingBox.getMinZ();
        int maxX = (int) boundingBox.getMaxX();
        int maxZ = (int) boundingBox.getMaxZ();

        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.WHITE, 5f);

        this.spawnAlongLine(minX, minZ, maxX, minZ, dustOptions); // TOP
        this.spawnAlongLine(minX, maxZ, maxX, maxZ, dustOptions); // BOTTOM
        this.spawnAlongLine(minX, minZ, minX, maxZ, dustOptions); // LEFT
        this.spawnAlongLine(maxX, minZ, maxX, maxZ, dustOptions); // RIGHT
    }

    private void spawnAlongLine(int x1, int z1, int x2, int z2, Particle.DustOptions dustOptions) {
        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;

        while (true) {
            Location location = new Location(this.player.getLocation().getWorld(), x1 + .5, this.player.getLocation().getY(), z1 + .5);
            this.player.spawnParticle(Particle.DUST, location, 1, dustOptions);

            if (x1 == x2 && z1 == z2) break;
            int e2 = 2 * err;
            if (e2 > -dz) {
                err -= dz;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                z1 += sz;
            }
        }
    }
}
