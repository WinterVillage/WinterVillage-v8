package de.wintervillage.main.calendar.listener;

import de.wintervillage.common.paper.item.ItemBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.calendar.CalendarDay;
import de.wintervillage.main.calendar.CalendarInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

import static de.wintervillage.common.paper.util.InventoryModifications.*;

public class InventoryClickListener implements Listener {

    private final WinterVillage winterVillage;

    public InventoryClickListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);
    }

    @EventHandler
    public void execute(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder(false) instanceof CalendarInventory calendarInventory)) return;

        // block disabled actions
        if (isPlacingItem(event) || isTakingItem(event) || isSwappingItem(event) || isDroppingItem(event) || isOtherEvent(event)) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }

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

        List<UUID> opened = calendarDay.get().opened();
        if (opened.contains(event.getWhoClicked().getUniqueId())) {
            event.getWhoClicked().sendMessage(Component.text("Already opened"));
            return;
        }

        this.winterVillage.calendarDatabase.modify(day, consumer -> {
                    consumer.addOpened(event.getWhoClicked().getUniqueId());
                })
                .thenAccept((v) -> {
                    this.winterVillage.calendarHandler.forceUpdate();

                    ItemStack reward = calendarDay.get().itemStack();
                    ((Player) event.getWhoClicked()).getInventory().addItem(reward);

                    event.setCurrentItem(ItemBuilder.from(Material.ORANGE_STAINED_GLASS_PANE)
                            .name(Component.text("Türchen " + day + " bereits geöffnet", NamedTextColor.GOLD))
                            .persistentDataContainer(pdc -> pdc.set(this.winterVillage.calendarKey, PersistentDataType.INTEGER, day))
                            .build());

                    event.getWhoClicked().sendMessage(Component.text("Opened", NamedTextColor.GREEN));
                    // TODO: sound
                })
                .exceptionally((t) -> {
                    event.getWhoClicked().sendMessage(Component.text("Could not open: " + t.getMessage()));
                    return null;
                });
    }
}
