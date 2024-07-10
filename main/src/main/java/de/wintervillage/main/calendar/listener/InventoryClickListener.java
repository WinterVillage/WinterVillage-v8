package de.wintervillage.main.calendar.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.calendar.CalendarDay;
import de.wintervillage.main.calendar.CalendarInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final WinterVillage winterVillage;

    public InventoryClickListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder(false) instanceof CalendarInventory calendarInventory)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        ItemStack clicked = event.getCurrentItem();

        if (!this.winterVillage.calendarHandler.withinRange()) {
            event.getWhoClicked().sendMessage(Component.text("Not available"));
            return;
        }

        if (!clicked.getPersistentDataContainer().has(this.winterVillage.calendarKey, PersistentDataType.INTEGER))
            return;
        int day = clicked.getPersistentDataContainer().get(this.winterVillage.calendarKey, PersistentDataType.INTEGER);
        Optional<CalendarDay> calendarDay = this.winterVillage.calendarHandler.byDay(day);

        if (!calendarDay.isPresent() || !this.winterVillage.calendarHandler.obtainable(day)) {
            event.getWhoClicked().sendMessage(Component.text("Unable to open"));
            return;
        }

        if (calendarDay.get().getOpened().contains(event.getWhoClicked().getUniqueId())) {
            event.getWhoClicked().sendMessage(Component.text("Already opened"));
            return;
        }

        List<UUID> opened = calendarDay.get().getOpened();
        opened.add(event.getWhoClicked().getUniqueId());

        this.winterVillage.calendarDatabase.updateOpened(day, opened)
                .thenAccept((v) -> {
                    calendarDay.get().setOpened(opened);

                    ItemStack reward = calendarDay.get().getItemStack();
                    ((Player) event.getWhoClicked()).getInventory().addItem(reward);

                    event.getWhoClicked().sendMessage(Component.text("Opened", NamedTextColor.GREEN));
                })
                .exceptionally((t) -> {
                    event.getWhoClicked().sendMessage(Component.text("Could not open: " + t.getMessage()));
                    return null;
                });
    }
}
