package de.wintervillage.main.plot.listener.setup;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

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
}
