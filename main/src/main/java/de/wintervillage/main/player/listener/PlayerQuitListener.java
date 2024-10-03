package de.wintervillage.main.player.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.player.PlayerHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerQuitListener implements Listener {

    private final WinterVillage winterVillage;
    private final PlayerHandler playerHandler;

    public PlayerQuitListener(PlayerHandler playerHandler) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.playerHandler = playerHandler;

        this.winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // HIGHEST, so every listener can modify the player before he's being saved
    public void execute(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        // delete scoreboard data
        this.winterVillage.scoreboardHandler.removeScoreboard(player.getUniqueId());

        // Skipping saving the player because he's still being loaded
        if (player.getPersistentDataContainer().has(this.playerHandler.applyingKey)) {
            player.getPersistentDataContainer().remove(this.playerHandler.applyingKey);
            return;
        }

        // save player
        this.playerHandler.save(player);
    }
}
