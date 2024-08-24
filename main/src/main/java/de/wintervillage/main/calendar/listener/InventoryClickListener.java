package de.wintervillage.main.calendar.listener;

import de.wintervillage.common.paper.item.ItemBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.calendar.CalendarDay;
import de.wintervillage.main.calendar.CalendarInventory;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
            String formatted = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(this.winterVillage.calendarHandler.startDate);

            event.getWhoClicked().sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.calendar.not-available", Component.text(formatted))
            ));
            return;
        }

        if (!clicked.getPersistentDataContainer().has(this.winterVillage.calendarKey, PersistentDataType.INTEGER))
            return;
        int day = clicked.getPersistentDataContainer().get(this.winterVillage.calendarKey, PersistentDataType.INTEGER);
        Optional<CalendarDay> calendarDay = this.winterVillage.calendarHandler.byDay(day);

        if (!calendarDay.isPresent() || !this.winterVillage.calendarHandler.obtainable(day)) {
            String formatted = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDate.of(2024, 12, day));

            event.getWhoClicked().sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.calendar.door-not-available-yet", Component.text(formatted))
            ));
            return;
        }

        List<UUID> opened = calendarDay.get().opened();
        if (opened.contains(event.getWhoClicked().getUniqueId())) {
            event.getWhoClicked().sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.calendar.day-already-redeemed")
            ));
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

                    event.getWhoClicked().sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.calendar.day-redeemed", Component.text(day))
                    ));
                    ((Player) event.getWhoClicked()).playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 2.0f, 0.4f));
                })
                .exceptionally((t) -> {
                    event.getWhoClicked().sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.calendar.could-not-redeem", Component.text(t.getMessage()))
                    ));
                    return null;
                });
    }
}
