package de.wintervillage.main.plot.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockBreakListener implements Listener {

    private final WinterVillage winterVillage;

    public BlockBreakListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(ignoreCancelled = true)
    public void execute(BlockBreakEvent event) {
        Player player = event.getPlayer();

        Plot plot = this.winterVillage.plotHandler.byBounds(event.getBlock().getLocation());
        if (plot == null) return;

        // TODO: LuckPerms
        if (!plot.getOwner().equals(player.getUniqueId())
                && !plot.getMembers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("You are not allowed to break this block", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("You are allowed to break this block", NamedTextColor.GREEN));
    }
}
