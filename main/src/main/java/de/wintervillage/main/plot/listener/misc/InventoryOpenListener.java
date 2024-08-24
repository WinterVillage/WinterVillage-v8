package de.wintervillage.main.plot.listener.misc;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryOpenListener implements Listener {

    private final WinterVillage winterVillage;

    public InventoryOpenListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();

        if (!(event.getInventory().getHolder(true) instanceof Villager villager)) return;

        Plot plot = this.winterVillage.plotHandler.byBounds(villager.getLocation());
        if (plot == null) return;

        if (player.hasPermission("wintervillage.plot.bypass")) return;

        if (plot.owner().equals(player.getUniqueId()) || plot.members().contains(player.getUniqueId())) return;

        // cancel open inventories of villagers in plots that the player is not a member of
        event.setCancelled(true);
        this.winterVillage.plotHandler.deny(player, villager.getLocation());
    }
}
