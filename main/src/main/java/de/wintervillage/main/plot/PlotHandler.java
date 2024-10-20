package de.wintervillage.main.plot;

import de.wintervillage.common.paper.item.ItemBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.combined.PlotUsers;
import de.wintervillage.main.plot.listener.block.*;
import de.wintervillage.main.plot.listener.entity.*;
import de.wintervillage.main.plot.listener.misc.InventoryMoveItemListener;
import de.wintervillage.main.plot.listener.misc.InventoryOpenListener;
import de.wintervillage.main.plot.listener.player.*;
import de.wintervillage.main.plot.listener.setup.CancelSetupListener;
import de.wintervillage.main.plot.task.BoundariesTask;
import de.wintervillage.main.plot.task.SetupTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlotHandler {

    private final WinterVillage winterVillage;

    public final List<Plot> plotCache;
    private final ScheduledExecutorService executorService;

    /**
     * Area of the plot will be MAX_PLOT_WIDTH x MAX_PLOT_WIDTH
     */
    public final int MAX_PLOT_WIDTH = 50;

    public final ItemStack SETUP_ITEM;

    public NamespacedKey setupBoundingsKey, setupTaskId, showBoundingsKey;

    public PlotHandler() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.plotCache = new ArrayList<>();

        this.setupBoundingsKey = new NamespacedKey("wintervillage", "plot/setup");
        this.setupTaskId = new NamespacedKey("wintervillage", "plot/setup_rectangle_task");
        this.showBoundingsKey = new NamespacedKey("wintervillage", "plot/setup_boundaries_task");

        this.SETUP_ITEM = ItemBuilder.from(Material.WOODEN_AXE)
                .name(Component.text("Mark your plot corners", NamedTextColor.GREEN))
                .persistentDataContainer(persistent -> persistent.set(this.setupBoundingsKey, PersistentDataType.BOOLEAN, true))
                .build();

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::forceUpdate, 0, 30, TimeUnit.SECONDS);

        // block
        new BlockBreakListener();
        new BlockFadeListener();
        new BlockFromToListener();
        new BlockPistonExtendListener();
        new BlockPistonRetractListener();
        new BlockPlaceListener();
        new de.wintervillage.main.plot.listener.block.PlayerInteractListener();
        new SignChangeListener();
        new StructureGrowListener();

        // entity
        new EntityBlockFormListener();
        new EntityExplodeListener();
        new EntityMountListener();
        new HangingBreakByEntityListener();
        new HangingPlaceListener();
        new VehicleDestroyListener();

        // misc
        new InventoryMoveItemListener();
        new InventoryOpenListener();

        // player
        new PlayerArmorStandManipulateListener();
        new PlayerBucketEmptyListener();
        new PlayerBucketFillListener();
        new PlayerInteractAtEntityListener(); // the client may send PlayerInteractEntityEvent in addition, thanks mojang
        new PlayerInteractEntityListener();
        new PlayerTNTListener();

        // setup
        new CancelSetupListener();
        new de.wintervillage.main.plot.listener.setup.PlayerInteractListener();
    }

    public void forceUpdate() {
        this.winterVillage.plotDatabase.find()
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

    public boolean exists(UUID uniqueId) {
        return this.plotCache.stream().anyMatch(plot -> plot.uniqueId().equals(uniqueId));
    }

    public Collection<Plot> byOwner(UUID owner) {
        return this.plotCache.stream()
                .filter(plot -> plot.owner().equals(owner))
                .toList();
    }

    public Plot byUniqueId(UUID uniqueId) {
        return this.plotCache.stream()
                .filter(plot -> plot.uniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public Plot byBounds(Location location) {
        return this.plotCache.stream()
                .filter(plot -> plot.boundingBox().contains(location.getBlockX(), location.getBlockZ()))
                .findFirst()
                .orElse(null);
    }

    public void terminate() {
        if (!this.executorService.isShutdown()) this.executorService.shutdown();
        Bukkit.getOnlinePlayers().forEach(this::stopTasks);
    }

    public boolean stopTasks(Player player) {
        boolean successful = false;

        PersistentDataContainer container = player.getPersistentDataContainer();
        // contains BoundingBox2D
        if (container.has(this.setupBoundingsKey)) {
            container.remove(this.setupBoundingsKey);
            successful = true;
        }

        // contains SetupTask taskId
        if (container.has(this.setupTaskId)) {
            int taskId = container.get(this.setupTaskId, PersistentDataType.INTEGER);

            SetupTask task = SetupTask.task(taskId);
            if (task != null) task.stop();

            container.remove(this.setupTaskId);
            successful = true;
        }

        // contains BoundariesTask taskId
        if (container.has(this.showBoundingsKey)) {
            int taskId = container.get(this.showBoundingsKey, PersistentDataType.INTEGER);

            BoundariesTask task = BoundariesTask.task(taskId);
            if (task != null) task.stop();

            container.remove(this.showBoundingsKey);
            successful = true;
        }

        // remove setup item
        boolean removedItem = Arrays.stream(player.getInventory().getContents())
                .filter(item -> item != null && item.hasItemMeta() && item.getPersistentDataContainer().has(this.setupBoundingsKey))
                .peek(itemStack -> player.getInventory().remove(itemStack))
                .count() > 0;
        if (removedItem) successful = true;

        return successful;
    }

    public void deny(Player player, Location location) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 4);
        player.spawnParticle(Particle.DUST, location.add(0.5, 0, 0.5), 10, 0, 0, 0, 0, dustOptions);
    }

    public CompletableFuture<PlotUsers> lookupUsers(Plot plot) {
        CompletableFuture<User> ownerFuture = this.winterVillage.luckPerms.getUserManager().loadUser(plot.owner());

        List<CompletableFuture<User>> memberFutures = plot.members().stream()
                .map(this.winterVillage.luckPerms.getUserManager()::loadUser)
                .toList();

        CompletableFuture<List<User>> membersFuture = CompletableFuture
                .allOf(memberFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> memberFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );

        return ownerFuture.thenCombine(membersFuture, PlotUsers::new)
                .exceptionally(throwable -> {
                    throw new RuntimeException("Could not load users for plot " + plot.uniqueId(), throwable);
                });
    }

    public Component formatMembers(PlotUsers users) {
        if (users.members().isEmpty()) return Component.text("keine", NamedTextColor.RED);

        List<Component> components = users.members().stream()
                .map(this::formatUser)
                .toList();

        Component separator = Component.text(", ", NamedTextColor.DARK_GRAY);
        return Component.join(JoinConfiguration.separator(separator), components);
    }

    /**
     * Format the user with the highest group color
     * <p>
     * Note: User will be null if the user never joined the server
     *
     * @param user {@link User} to format
     * @return {@link Component} with the highest group color
     */
    private Component formatUser(User user) {
        if (user.getUsername() == null) return Component.text("unknown", NamedTextColor.RED)
                .hoverEvent(HoverEvent.showText(Component.text("UUID: " + user.getUniqueId().toString(), NamedTextColor.RED)));

        Group highestGroup = this.winterVillage.playerHandler.highestGroup(user);
        return MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + user.getUsername());
    }

    public List<Plot> getPlotCache() {
        return new ArrayList<>(this.plotCache);
    }
}
