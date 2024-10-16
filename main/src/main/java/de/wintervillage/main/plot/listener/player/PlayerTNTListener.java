package de.wintervillage.main.plot.listener.player;

import com.jeff_media.customblockdata.CustomBlockData;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

public class PlayerTNTListener implements Listener {

    private final WinterVillage winterVillage;
    private final NamespacedKey creatorKey = new NamespacedKey("wintervillage", "plot/block_creator");

    public PlayerTNTListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void execute(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (event.getBlockPlaced().getType() != Material.TNT) return;

        Player player = event.getPlayer();
        if (player.hasPermission("wintervillage.plot.bypass")) return;

        PersistentDataContainer container = new CustomBlockData(event.getBlockPlaced(), this.winterVillage);
        container.set(this.creatorKey, PersistentDataType.STRING, player.getUniqueId().toString());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void execute(TNTPrimeEvent event) {
        PersistentDataContainer blockContainer = new CustomBlockData(event.getBlock(), this.winterVillage);
        if (!blockContainer.has(this.creatorKey)) return;

        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);

        UUID creator = UUID.fromString(blockContainer.get(this.creatorKey, PersistentDataType.STRING));

        event.getBlock().getWorld().spawn(event.getBlock().getLocation(), TNTPrimed.class, tntPrimed -> {
            PersistentDataContainer entityContainer = tntPrimed.getPersistentDataContainer();
            entityContainer.set(this.creatorKey, PersistentDataType.STRING, creator.toString());
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST) // called last
    public void execute(EntityExplodeEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof TNTPrimed tntPrimed)) return;

        Optional<Plot> optionalPlot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(tntPrimed.getLocation()));
        if (optionalPlot.isEmpty()) {
            event.blockList().removeIf(block -> this.winterVillage.plotHandler.byBounds(tntPrimed.getLocation()) != null);
            return;
        }

        // explosion in inner plot
        Plot plot = optionalPlot.get();

        PersistentDataContainer container = tntPrimed.getPersistentDataContainer();
        if (!container.has(this.creatorKey)) return;

        UUID creator = UUID.fromString(container.get(this.creatorKey, PersistentDataType.STRING));
        if (plot.owner().equals(creator) || plot.members().contains(creator)) return;

        // remove blocks of exploded tnt, which was manipulated by a player due to chain reaction
        event.blockList().removeIf(block -> plot.boundingBox().contains(block.getLocation().getBlockX(), block.getLocation().getBlockZ()));
    }
}
