package de.wintervillage.main.plot;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.listener.BlockBreakListener;
import org.bukkit.Location;
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

    public List<Plot> plotCache;
    private final ScheduledExecutorService executorService;

    private final String CHARACTERS = "ABDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final Random random = new Random();

    /**
     * Area of the plot will be MAX_PLOT_SIZE x MAX_PLOT_SIZE
     */
    public static final int MAX_PLOT_SIZE = 50;

    public PlotHandler() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.plotCache = new ArrayList<>();

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::forceUpdate, 0, 30, TimeUnit.SECONDS);

        new BlockBreakListener();
    }

    public void forceUpdate() {
        this.winterVillage.plotDatabase.findAsync()
                .thenAccept(plots -> {
                    this.plotCache.clear();
                    this.plotCache.addAll(plots);
                })
                .exceptionally(t -> {
                    this.winterVillage.getLogger().warning("Could not load plots: " + t.getMessage());
                    return null;
                });
    }

    public boolean exists(String uniqueId) {
        return this.plotCache.stream().anyMatch(plot -> plot.getUniqueId().equals(uniqueId));
    }

    public List<Plot> getPlotByOwner(UUID owner) {
        return this.plotCache.stream()
                .filter(plot -> plot.getOwner().equals(owner))
                .toList();
    }

    public Plot getPlotById(String uniqueId) {
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
    }

    public String generateId(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(this.CHARACTERS.charAt(this.random.nextInt(this.CHARACTERS.length())));
        }
        return builder.toString();
    }
}
