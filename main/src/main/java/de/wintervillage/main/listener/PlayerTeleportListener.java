package de.wintervillage.main.listener;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerTeleportListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerTeleportListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void execute(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
                && event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.join(
                this.winterVillage.prefix,
                Component.translatable("wintervillage.world-teleportation-is-disabled")
        ));
    }
}
