package de.wintervillage.main.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerJoinListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void execute(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        this.winterVillage.scoreboardHandler.playerList(player);
    }
}
