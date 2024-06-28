package de.wintervillage.main.plot;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.util.BoundingBox2D;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ParticleRectangle implements Runnable {

    private final WinterVillage winterVillage;

    private final Player player;
    private int taskId;
    private BoundingBox2D boundingBox;

    public static Map<Integer, ParticleRectangle> RECTANGLES = new HashMap<>();

    public ParticleRectangle(Player player) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.player = player;
    }

    public int start() {
        this.taskId = Bukkit.getScheduler().runTaskTimer(this.winterVillage, this, 0L, 20L).getTaskId();
        RECTANGLES.put(this.taskId, this);
        return this.taskId;
    }

    public void setBoundingBox(BoundingBox2D boundingBox) {
        this.boundingBox = boundingBox;
    }

    public void stop() {
        RECTANGLES.remove(this.taskId);
        if (Bukkit.getScheduler().isCurrentlyRunning(this.taskId)) Bukkit.getScheduler().cancelTask(this.taskId);
    }

    @Override
    public void run() {
        if (this.boundingBox == null) return;

        World world = Bukkit.getWorld("world");
        if (world == null) return;

        int minX = (int) this.boundingBox.getMinX();
        int minZ = (int) this.boundingBox.getMinZ();
        int maxX = (int) this.boundingBox.getMaxX();
        int maxZ = (int) this.boundingBox.getMaxZ();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x == minX || x == maxX || z == minZ || z == maxZ) {
                    Location location = new Location(world, x + .5, this.player.getLocation().getY(), z + .5);
                    this.player.spawnParticle(Particle.HEART, location, 1);
                }
            }
        }
    }
}
