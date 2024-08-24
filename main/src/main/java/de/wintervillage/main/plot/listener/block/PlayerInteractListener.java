package de.wintervillage.main.plot.listener.block;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerInteractListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerInteractListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    // event gets called twice because EquipmentSlot.HAND & EquipmentSlot.OFF_HAND , thanks mojang
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        // ignore non-interactable blocks
        if (!(clickedBlock.getType().isInteractable() && clickedBlock.getType().isBlock())) return;

        Plot plot = this.winterVillage.plotHandler.byBounds(clickedBlock.getLocation());
        if (plot == null) return;

        if (player.hasPermission("wintervillage.plot.bypass")) return;

        if (plot.owner().equals(player.getUniqueId()) || plot.members().contains(player.getUniqueId())) return;

        // cancel interactions with blocks in plots that the player is not a member of
        // event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        this.winterVillage.plotHandler.deny(player, clickedBlock.getLocation());
    }
}
