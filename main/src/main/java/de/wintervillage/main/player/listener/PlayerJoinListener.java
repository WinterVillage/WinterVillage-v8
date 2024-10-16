package de.wintervillage.main.player.listener;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
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
        this.winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        Group highestGroup = this.winterVillage.playerHandler.highestGroup(player);
        event.joinMessage(Component.translatable("wintervillage.player-join",
                MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + player.getName())
        ));

        // player data being loaded
        this.winterVillage.playerHandler.apply(player, player.getUniqueId());

        // scoreboard
        this.winterVillage.scoreboardHandler.playerList();

        // handle pending home & farmwelt teleportation requests
        this.winterVillage.playerHandler.channelMessageListener.processRequest(player);
    }
}
