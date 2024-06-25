package de.wintervillage.main.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerMoveListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerMoveListener(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // TODO: check permission with LuckPerms too
        boolean isFrozen = player.getPersistentDataContainer().getOrDefault(this.winterVillage.frozenKey, PersistentDataType.BOOLEAN, false);
        if (this.winterVillage.PLAYERS_FROZEN || isFrozen) this.deny(player, event.getFrom(), event.getTo());
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
