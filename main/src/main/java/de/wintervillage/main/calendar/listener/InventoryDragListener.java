package de.wintervillage.main.calendar.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.calendar.CalendarInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import static de.wintervillage.main.util.InventoryModifications.isDraggingItem;

public class InventoryDragListener implements Listener {

    private final WinterVillage winterVillage;

    public InventoryDragListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);
    }

    @EventHandler
    public void execute(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder(false) instanceof CalendarInventory calendarInventory)) return;

        if (!isDraggingItem(event)) return;

        event.setCancelled(true);
        event.setResult(InventoryDragEvent.Result.DENY);
    }
}
