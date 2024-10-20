package de.wintervillage.main.listener;

import de.wintervillage.main.WinterVillage;
import net.luckperms.api.model.group.Group;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerMoveListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerMoveListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        boolean isFrozen = player.getPersistentDataContainer().getOrDefault(this.winterVillage.frozenKey, PersistentDataType.BOOLEAN, false);
        if (this.winterVillage.PLAYERS_FROZEN || isFrozen) {
            Group highestGroup = this.winterVillage.playerHandler.highestGroup(player);
            if (highestGroup.getWeight().getAsInt() >= 200) return;

            this.deny(player, event.getFrom(), event.getTo());
        }
    }

    private void deny(Player player, Location from, Location to) {
        double x = Math.floor(from.getX());
        double z = Math.floor(from.getZ());

        if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
            x += .5;
            z += .5;

            player.teleport(new Location(from.getWorld(), x, from.getY(), z, from.getYaw(), from.getPitch()));
            player.teleport(player);
        }
    }
}
