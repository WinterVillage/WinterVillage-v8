package de.wintervillage.main.plot.listener.block;

import de.wintervillage.main.WinterVillage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockFadeListener implements Listener {

    private final WinterVillage winterVillage;

    public BlockFadeListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void execute(BlockFadeEvent event) {
        // TODO: snow melting, ice melting, fire burning out, coral dying,
    }
}
