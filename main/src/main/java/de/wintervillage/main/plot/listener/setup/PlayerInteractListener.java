package de.wintervillage.main.plot.listener.setup;

import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.common.paper.persistent.BoundingBoxDataType;
import de.wintervillage.main.plot.Plot;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
        event.setUseInteractedBlock(Event.Result.DENY);

        BoundingBox2D boundingBox = player.getPersistentDataContainer().get(this.winterVillage.plotHandler.plotSetupKey, new BoundingBoxDataType());

        if (event.getAction().isLeftClick()) {
            boundingBox.setMinX(event.getClickedBlock().getX());
            boundingBox.setMinZ(event.getClickedBlock().getZ());
        }

        if (event.getAction().isRightClick()) {
            boundingBox.setMaxX(event.getClickedBlock().getX());
            boundingBox.setMaxZ(event.getClickedBlock().getZ());
        }

        boolean intersects = this.winterVillage.plotHandler.getPlotCache().stream()
                .map(Plot::boundingBox)
                .anyMatch(plot -> plot.intersects(boundingBox));
        if (intersects) {
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.plot.bounding-cannot-intersect")
            ));
            return;
        }

        player.sendMessage(Component.join(
                this.winterVillage.prefix,
                Component.translatable("wintervillage.plot.set-bounding",
                        Component.text(event.getAction().isLeftClick() ? "1" : "2"),
                        Component.text(event.getClickedBlock().getX() + ", " + event.getClickedBlock().getZ())
                )
        ));

        player.getPersistentDataContainer().set(this.winterVillage.plotHandler.plotSetupKey, new BoundingBoxDataType(), boundingBox);
    }
}
