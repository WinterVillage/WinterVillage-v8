package de.wintervillage.main.plot.listener.setup;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import static de.wintervillage.common.paper.util.InventoryModifications.*;

public class CancelSetupListener implements Listener {

    private final WinterVillage winterVillage;

    public CancelSetupListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.winterVillage.plotHandler.stopTasks(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (this.winterVillage.plotHandler.stopTasks(player))
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.plot.setup-cancelled")
            ));
    }

    @EventHandler
    public void execute(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        ItemStack itemStack = event.getItemDrop().getItemStack();
        if (!itemStack.isSimilar(this.winterVillage.plotHandler.SETUP_ITEM)) return;

        event.getItemDrop().remove();

        if (this.winterVillage.plotHandler.stopTasks(player))
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.plot.setup-cancelled")
            ));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void execute(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();


        if (dropsWrongItem(event, this.winterVillage.plotHandler.setupBoundingsKey)
                || placesWrongItem(event, this.winterVillage.plotHandler.setupBoundingsKey)
                || swapsWrongItem(event, this.winterVillage.plotHandler.setupBoundingsKey)
                || otherWrongEvent(event, this.winterVillage.plotHandler.setupBoundingsKey)) {
            if (!this.winterVillage.plotHandler.stopTasks(player)) return;

            event.setCurrentItem(null);
            event.setCursor(null);

            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.plot.setup-cancelled")
            ));
        }
    }
}
