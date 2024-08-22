package de.wintervillage.main.plot.listener.entity;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class EntityBlockFormListener implements Listener {

    private final WinterVillage winterVillage;

    public EntityBlockFormListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(EntityBlockFormEvent event) {
        Entity entity = event.getEntity();

        Optional<Plot> plot = Optional.ofNullable(this.winterVillage.plotHandler.byBounds(event.getBlock().getLocation()));
        if (plot.isEmpty()) return;

        if (entity instanceof Player player) {
            if (player.hasPermission("wintervillage.plot.bypass")) return;

            if (plot.get().owner().equals(player.getUniqueId()) || plot.get().members().contains(player.getUniqueId())) return;

            // cancel block forms in plots that the player is not a member of
            event.setCancelled(true);
            player.sendMessage(Component.text("You are not allowed to form this block", NamedTextColor.RED));
        }

        // TODO: snowgolem forming snow
    }
}
