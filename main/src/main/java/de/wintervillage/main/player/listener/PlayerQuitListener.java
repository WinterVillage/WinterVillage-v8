package de.wintervillage.main.player.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerQuitListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerQuitListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void execute(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        this.winterVillage.playerDatabase.modify(player.getUniqueId(), winterVillagePlayer -> {
                winterVillagePlayer.playerInformation().save(player);
            })
            .exceptionally(throwable -> {
                this.winterVillage.getLogger().severe(
                        String.format("There was an error while saving the data from [%1$s, %2$s] : %3$s", player.getUniqueId(), player.getName(), throwable.getMessage())
                );
                return null;
            });
    }
}
