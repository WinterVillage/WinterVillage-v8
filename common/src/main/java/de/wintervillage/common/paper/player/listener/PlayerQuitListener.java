package de.wintervillage.common.paper.player.listener;

import de.wintervillage.common.paper.player.PlayerHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerQuitListener implements Listener {

    private final JavaPlugin javaPlugin;
    private final PlayerHandler playerHandler;

    public PlayerQuitListener(JavaPlugin javaPlugin, PlayerHandler playerHandler) {
        this.javaPlugin = javaPlugin;
        this.playerHandler = playerHandler;

        this.javaPlugin.getServer().getPluginManager().registerEvents(this, this.javaPlugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // HIGHEST, so every listener can modify the player before he's being saved
    public void execute(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        // Skipping saving the player because he's still being loaded
        if (player.getPersistentDataContainer().has(this.playerHandler.applyingKey)) {
            player.getPersistentDataContainer().remove(this.playerHandler.applyingKey);
            return;
        }

        this.playerHandler.save(player);
    }
}
