package de.wintervillage.main.player.listener.luckperms;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Registers the {@link UserDataRecalculateEvent} event which gets fired when a user's data is recalculated.
 * This is used to update the scoreboard to stay up-to-date
 */
public class UserRecalculation {

    private final WinterVillage winterVillage;

    public UserRecalculation() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        EventBus eventBus = this.winterVillage.luckPerms.getEventBus();
        eventBus.subscribe(this.winterVillage, UserDataRecalculateEvent.class, this::recalculating);
    }

    private void recalculating(UserDataRecalculateEvent event) {
        Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
        if (player == null) return;

        Group highestGroup = this.winterVillage.playerHandler.highestGroup(player);
        Bukkit.getScheduler().runTask(this.winterVillage, () -> {
            this.winterVillage.scoreboardHandler.playerList();

            this.winterVillage.scoreboardHandler.updateScore(
                    player,
                    "10_highestgroup-value",
                    Component.space().append(MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getPrefix()))
            );
        });
    }
}
