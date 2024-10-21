package de.wintervillage.main.player.listener;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
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
        this.winterVillage.scoreboardHandler.sidebar(player);
        this.winterVillage.scoreboardHandler.playerList();

        this.winterVillage.scoreboardHandler.updateScore(
                "04_online-value",
                Component.space().append(Component.text(Bukkit.getOnlinePlayers().size(), NamedTextColor.GREEN))
        );
        this.winterVillage.scoreboardHandler.updateScore(
                player,
                "10_highestgroup-value",
                Component.space().append(MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getPrefix()))
        );
        this.winterVillage.playerDatabase.player(player.getUniqueId())
                .thenAccept(winterVillagePlayer -> {
                    Bukkit.getScheduler().runTask(this.winterVillage, () -> this.winterVillage.scoreboardHandler.updateScore(
                            player,
                            "07_balance-value",
                            Component.space().append(Component.text(this.winterVillage.formatBD(winterVillagePlayer.money(), true) + " $", NamedTextColor.YELLOW)))
                    );
                });

        // handle pending home & farmwelt teleportation requests
        this.winterVillage.playerHandler.channelMessageListener.processRequest(player);

        // resource pack
        player.setResourcePack(
                "https://voldechse.wtf/wintervillage.zip",
                "8679cec65670db5b708cabe94568c99a4ffe8650",
                true,
                Component.text("In order to play on this server, you must accept the resource pack.", NamedTextColor.YELLOW)
        );
    }
}
