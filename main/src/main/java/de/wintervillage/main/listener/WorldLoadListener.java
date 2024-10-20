package de.wintervillage.main.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldLoadListener implements Listener {

    private final WinterVillage winterVillage;

    public WorldLoadListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void execute(WorldLoadEvent event) {
        event.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        event.getWorld().setGameRule(GameRule.KEEP_INVENTORY, false);
        event.getWorld().setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);

        if (!event.getWorld().getName().equalsIgnoreCase("world")) return;

        // load shops after world has been loaded successfully
        if (this.winterVillage.configDocument.getDocument("enabled").getBoolean("shop_handler")) {
            this.winterVillage.shopHandler.forceUpdate((success, message) -> {
                if (success) return;
                this.winterVillage.getLogger().warning("Shops could not be loaded: " + message);
            });
        }
    }
}
