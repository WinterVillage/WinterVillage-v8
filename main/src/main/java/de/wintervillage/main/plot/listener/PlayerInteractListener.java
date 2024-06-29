package de.wintervillage.main.plot.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.persistent.BoundingBoxDataType;
import de.wintervillage.main.util.BoundingBox2D;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerInteractListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerInteractListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(ignoreCancelled = true)
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() == null || event.getClickedBlock() == null) return;
        if (!event.getItem().getPersistentDataContainer().has(this.winterVillage.plotHandler.plotSetupKey)) return;

        if (!player.getPersistentDataContainer().has(this.winterVillage.plotHandler.plotSetupKey)) return;
        BoundingBox2D boundingBox2D = player.getPersistentDataContainer().get(this.winterVillage.plotHandler.plotSetupKey, new BoundingBoxDataType());

        if (event.getAction().isLeftClick()) {
            boundingBox2D.setMinX(event.getClickedBlock().getX());
            boundingBox2D.setMinZ(event.getClickedBlock().getZ());
        }

        if (event.getAction().isRightClick()) {
            boundingBox2D.setMaxX(event.getClickedBlock().getX());
            boundingBox2D.setMaxZ(event.getClickedBlock().getZ());
        }

        player.getPersistentDataContainer().set(this.winterVillage.plotHandler.plotSetupKey, new BoundingBoxDataType(), boundingBox2D);
    }
}
