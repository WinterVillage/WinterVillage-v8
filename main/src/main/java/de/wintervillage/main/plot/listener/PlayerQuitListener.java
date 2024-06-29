package de.wintervillage.main.plot.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.ParticleRectangle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class PlayerQuitListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerQuitListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(ignoreCancelled = true)
    public void execute(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (player.getPersistentDataContainer().has(this.winterVillage.plotHandler.plotSetupKey))
            player.getPersistentDataContainer().remove(this.winterVillage.plotHandler.plotSetupKey);

        if (player.getPersistentDataContainer().has(this.winterVillage.plotHandler.plotRectangleKey)) {
            int taskId = player.getPersistentDataContainer().get(this.winterVillage.plotHandler.plotRectangleKey, PersistentDataType.INTEGER);
            ParticleRectangle.getRectangle(taskId).stop();
            player.getPersistentDataContainer().remove(this.winterVillage.plotHandler.plotRectangleKey);
        }

        // Remove setup plot item
        Arrays.stream(player.getInventory().getContents())
                .filter(item -> item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(this.winterVillage.plotHandler.plotSetupKey))
                .forEach(item -> player.getInventory().remove(item));
    }
}
