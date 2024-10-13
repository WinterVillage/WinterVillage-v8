package de.wintervillage.main.player.listener.luckperms;

import de.wintervillage.main.WinterVillage;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
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
        if (Bukkit.getPlayer(event.getUser().getUniqueId()) == null) return;
        this.winterVillage.scoreboardHandler.playerList();
    }
}
