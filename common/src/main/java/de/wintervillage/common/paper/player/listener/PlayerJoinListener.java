package de.wintervillage.common.paper.player.listener;

import de.wintervillage.common.paper.player.PlayerHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

    private final JavaPlugin javaPlugin;
    private final PlayerHandler playerHandler;

    public PlayerJoinListener(JavaPlugin javaPlugin, PlayerHandler playerHandler) {
        this.javaPlugin = javaPlugin;
        this.playerHandler = playerHandler;

        this.javaPlugin.getServer().getPluginManager().registerEvents(this, this.javaPlugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.playerHandler.apply(player, player.getUniqueId());
    }
}
