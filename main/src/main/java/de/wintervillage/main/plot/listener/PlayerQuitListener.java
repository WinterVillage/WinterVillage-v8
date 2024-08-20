package de.wintervillage.main.plot.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerQuitListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerQuitListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.winterVillage.plotHandler.stopSetup(player);
    }
}
