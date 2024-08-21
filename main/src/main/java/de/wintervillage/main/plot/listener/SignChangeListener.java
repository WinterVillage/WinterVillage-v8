package de.wintervillage.main.plot.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SignChangeListener implements Listener {

    private final WinterVillage winterVillage;

    public SignChangeListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(SignChangeEvent event) {
        Player player = event.getPlayer();

        Plot plot = this.winterVillage.plotHandler.byBounds(event.getBlock().getLocation());
        if (plot == null) return;

        if (player.hasPermission("wintervillage.plot.bypass")) return;

        if (!plot.owner().equals(player.getUniqueId())
                && !plot.members().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("You are not allowed to change this sign", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("You are allowed to change this sign", NamedTextColor.GREEN));
    }
}
