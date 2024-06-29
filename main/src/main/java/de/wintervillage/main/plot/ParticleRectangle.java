package de.wintervillage.main.plot;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.persistent.BoundingBoxDataType;
import de.wintervillage.main.util.BoundingBox2D;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ParticleRectangle implements Runnable {

    private final WinterVillage winterVillage;

    private final Player player;
    private int taskId;

    private static final Map<Integer, ParticleRectangle> RECTANGLES = new HashMap<>();

    public ParticleRectangle(Player player) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.player = player;
    }

    public int start() {
        this.taskId = Bukkit.getScheduler().runTaskTimer(this.winterVillage, this, 0L, 20L).getTaskId();
        RECTANGLES.put(this.taskId, this);
        return this.taskId;
    }

    public void stop() {
        RECTANGLES.remove(this.taskId);
        if (Bukkit.getScheduler().isCurrentlyRunning(this.taskId)) Bukkit.getScheduler().cancelTask(this.taskId);
    }

    public static ParticleRectangle getRectangle(int taskId) {
        return RECTANGLES.get(taskId);
    }

    @Override
    public void run() {
        if (!this.player.getPersistentDataContainer().has(this.winterVillage.plotHandler.plotSetupKey)) return;
        BoundingBox2D boundingBox = this.player.getPersistentDataContainer().get(this.winterVillage.plotHandler.plotSetupKey, new BoundingBoxDataType());

        if (boundingBox.getMinX() == 0 || boundingBox.getMinZ() == 0 || boundingBox.getMaxX() == 0 || boundingBox.getMaxZ() == 0)
            return;

        int minX = (int) boundingBox.getMinX();
        int minZ = (int) boundingBox.getMinZ();
        int maxX = (int) boundingBox.getMaxX();
        int maxZ = (int) boundingBox.getMaxZ();

        boolean tooLarge = boundingBox.getWidthX() > this.winterVillage.plotHandler.MAX_PLOT_WIDTH
                || boundingBox.getWidthZ() > this.winterVillage.plotHandler.MAX_PLOT_WIDTH;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x == minX || x == maxX || z == minZ || z == maxZ) {
                    Location location = new Location(player.getLocation().getWorld(), x + .5, this.player.getLocation().getY(), z + .5);
                    this.player.spawnParticle(Particle.HEART, location, 1);
                }
            }
        }

        // TODO: LuckPerms (&& !this.player.hasPermission("..."))
        Component component = tooLarge
                ? Component.text("✘ ", NamedTextColor.RED).decoration(TextDecoration.BOLD, true)
                .append(Component.text("Selected area is too big ", NamedTextColor.RED))
                .append(Component.text("✘", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))
                : Component.text("Selected area: " + boundingBox.getWidthX() + "x" + boundingBox.getWidthZ(), NamedTextColor.GREEN);

        this.player.sendActionBar(component);
    }
}
