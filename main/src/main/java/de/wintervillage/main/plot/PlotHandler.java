package de.wintervillage.main.plot;

import de.wintervillage.common.paper.item.ItemBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.listener.BlockBreakListener;
import de.wintervillage.main.plot.listener.PlayerInteractListener;
import de.wintervillage.main.plot.listener.PlayerQuitListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlotHandler {

    private final WinterVillage winterVillage;

    public final List<Plot> plotCache;
    private final ScheduledExecutorService executorService;

    private final String CHARACTERS = "ABDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final Random random = new Random();

    /**
     * Area of the plot will be MAX_PLOT_WIDTH x MAX_PLOT_WIDTH
     */
    public final int MAX_PLOT_WIDTH = 50;

    public final ItemStack SETUP_ITEM;

    public NamespacedKey plotSetupKey, plotRectangleKey;

    public PlotHandler() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.plotCache = new ArrayList<>();

        this.plotSetupKey = new NamespacedKey("wintervillage", "plot_setup");
        this.plotRectangleKey = new NamespacedKey("wintervillage", "plot_rectangle");

        this.SETUP_ITEM = ItemBuilder.from(Material.WOODEN_AXE)
                .name(Component.text("Mark your plot corners", NamedTextColor.GREEN))
                .persistentDataContainer(persistent -> persistent.set(this.plotSetupKey, PersistentDataType.BOOLEAN, true))
                .build();

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::forceUpdate, 0, 30, TimeUnit.SECONDS);

        new BlockBreakListener();
        new PlayerInteractListener();
        new PlayerQuitListener();
    }

    public void forceUpdate() {
        this.winterVillage.plotDatabase.findAsync()
                .thenAccept(plots -> {
                    synchronized (this.plotCache) {
                        this.plotCache.clear();
                        this.plotCache.addAll(plots);
                    }
                })
                .exceptionally(t -> {
                    this.winterVillage.getLogger().warning("Could not load plots: " + t.getMessage());
                    return null;
                });
    }

    public boolean exists(String uniqueId) {
        return this.plotCache.stream().anyMatch(plot -> plot.getUniqueId().equals(uniqueId));
    }

    public List<Plot> byOwner(UUID owner) {
        return this.plotCache.stream()
                .filter(plot -> plot.getOwner().equals(owner))
                .toList();
    }

    public Plot byUniqueId(String uniqueId) {
        return this.plotCache.stream()
                .filter(plot -> plot.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public Plot byBounds(Location location) {
        return this.plotCache.stream()
                .filter(plot -> plot.getBoundingBox().contains(location.getBlockX(), location.getBlockZ()))
                .findFirst()
                .orElse(null);
    }

    public void terminate() {
        if (!this.executorService.isShutdown()) this.executorService.shutdown();

        Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.getPersistentDataContainer().has(this.plotSetupKey) || player.getPersistentDataContainer().has(this.plotRectangleKey))
                .forEach(player -> {
                    if (player.getPersistentDataContainer().has(this.plotSetupKey))
                        player.getPersistentDataContainer().remove(this.plotSetupKey);

                    if (player.getPersistentDataContainer().has(this.plotRectangleKey)) {
                        int taskId = player.getPersistentDataContainer().get(this.plotRectangleKey, PersistentDataType.INTEGER);
                        ParticleRectangle.getRectangle(taskId).stop();
                        player.getPersistentDataContainer().remove(this.plotRectangleKey);
                    }
                });
    }

    public String generateId(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(this.CHARACTERS.charAt(this.random.nextInt(this.CHARACTERS.length())));
        }
        return builder.toString();
    }
}
