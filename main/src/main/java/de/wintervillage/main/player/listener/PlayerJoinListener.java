package de.wintervillage.main.player.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.player.PlayerHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

    private final PlayerHandler playerHandler;

    public PlayerJoinListener(PlayerHandler playerHandler) {
        WinterVillage winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.playerHandler = playerHandler;

        winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.playerHandler.apply(player, player.getUniqueId());
    }
}
