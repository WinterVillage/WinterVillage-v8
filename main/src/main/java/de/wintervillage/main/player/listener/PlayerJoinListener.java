package de.wintervillage.main.player.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.player.PlayerHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static de.wintervillage.main.player.listener.HomeRequest.PENDING_HOME_REQUESTS;

public class PlayerJoinListener implements Listener {

    private final WinterVillage winterVillage;
    private final PlayerHandler playerHandler;

    public PlayerJoinListener(PlayerHandler playerHandler) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.playerHandler = playerHandler;

        this.winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.playerHandler.apply(player, player.getUniqueId());

        PENDING_HOME_REQUESTS.computeIfPresent(player.getUniqueId(), (uuid, homeInformation) -> {
            player.teleportAsync(
                    new Location(
                            Bukkit.getWorld(homeInformation.world()),
                            homeInformation.x(),
                            homeInformation.y(),
                            homeInformation.z()),
                    PlayerTeleportEvent.TeleportCause.PLUGIN
            );
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.command.home.teleported")
            ));
            return PENDING_HOME_REQUESTS.remove(uuid);
        });
    }
}
