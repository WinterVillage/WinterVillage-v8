package de.wintervillage.main.player.listener;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
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
        this.winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // HIGHEST, so every listener can modify the player before he's being saved
    public void execute(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        Group highestGroup = this.winterVillage.playerHandler.highestGroup(player);
        event.quitMessage(Component.translatable("wintervillage.player-quit",
                MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + player.getName())
        ));

        // delete scoreboard data
        this.winterVillage.scoreboardHandler.removeScoreboard(player.getUniqueId());

        // Skipping saving the player because he's still being loaded
        if (player.getPersistentDataContainer().has(this.winterVillage.playerHandler.applyingKey)) {
            player.getPersistentDataContainer().remove(this.winterVillage.playerHandler.applyingKey);
            return;
        }

        // save player
        this.winterVillage.playerHandler.save(player);
    }
}
