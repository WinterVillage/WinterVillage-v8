package de.wintervillage.main.calendar;

import de.wintervillage.common.paper.item.ItemBuilder;
import de.wintervillage.main.WinterVillage;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CalendarInventory {

    private final WinterVillage winterVillage;

    private final Gui gui;

    private static final ItemStack EMPTY = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).build();

    public CalendarInventory(Player opener) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.gui = Gui.gui()
                .rows(6)
                .disableAllInteractions()
                .title(Component.text("Adventskalender", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .create();
        this.gui.getFiller().fillBorder(new GuiItem(EMPTY));
        this.gui.setItem(2, 2, new GuiItem(EMPTY));
        this.gui.setItem(2, 8, new GuiItem(EMPTY));
        this.gui.setItem(5, 2, new GuiItem(EMPTY));
        this.gui.setItem(5, 8, new GuiItem(EMPTY));

        this.gui.setDefaultClickAction(event -> {
            Player player = (Player) event.getWhoClicked();

            ItemStack itemStack = event.getCurrentItem();
            if (itemStack == null) return;

            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
            if (!container.has(this.winterVillage.calendarKey, PersistentDataType.INTEGER)) return;

            int day = container.get(this.winterVillage.calendarKey, PersistentDataType.INTEGER);
            Optional<CalendarDay> calendarDay = this.winterVillage.calendarHandler.byDay(day);

            if (!calendarDay.isPresent() || !this.winterVillage.calendarHandler.obtainable(day)) {
                String formatted = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDate.of(2024, Month.DECEMBER, day));

                player.sendMessage(Component.join(
                        this.winterVillage.prefix,
                        Component.translatable("wintervillage.calendar.door-not-available-yet", Component.text(formatted))
                ));
                return;
            }

            List<UUID> opened = calendarDay.get().opened();
            if (opened.contains(player.getUniqueId())) {
                player.sendMessage(Component.join(
                        this.winterVillage.prefix,
                        Component.translatable("wintervillage.calendar.day-already-redeemed")
                ));
                return;
            }

            this.winterVillage.calendarDatabase.modify(day, consumer -> {
                        consumer.addOpened(player.getUniqueId());
                    })
                    .thenAccept((v) -> {
                        this.winterVillage.calendarHandler.forceUpdate();

                        ItemStack reward = calendarDay.get().itemStack();
                        player.getInventory().addItem(reward);

                        event.setCurrentItem(ItemBuilder.from(Material.ORANGE_STAINED_GLASS_PANE)
                                .name(Component.text("Türchen " + day + " bereits geöffnet", NamedTextColor.GOLD))
                                .persistentDataContainer(pdc -> pdc.set(this.winterVillage.calendarKey, PersistentDataType.INTEGER, day))
                                .build());

                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.calendar.day-redeemed", Component.text(day))
                        ));
                        player.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 2.0f, 0.4f));
                    })
                    .exceptionally((t) -> {
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.calendar.could-not-redeem", Component.text(t.getMessage()))
                        ));
                        return null;
                    });
        });

        int current = LocalDate.now().getDayOfMonth();

        // shuffle 1 - 24
        IntStream intStream = IntStream.rangeClosed(1, 24)
                .boxed()
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream();
                }))
                .mapToInt(i -> i);
        intStream.forEach(i -> {
            Optional<CalendarDay> optional = this.winterVillage.calendarHandler.byDay(i);
            if (optional.isEmpty() || current < i) {
                this.gui.addItem(new GuiItem(ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                        .name(Component.text("Türchen " + i + " verschlossen", NamedTextColor.RED))
                        .persistentDataContainer(pdc -> pdc.set(this.winterVillage.calendarKey, PersistentDataType.INTEGER, i))
                        .build()));
                return;
            }

            if (!optional.get().opened().contains(opener.getUniqueId())) {
                this.gui.addItem(new GuiItem(ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                        .name(Component.text("Türchen " + i + " öffnen", NamedTextColor.GREEN))
                        .persistentDataContainer(pdc -> pdc.set(this.winterVillage.calendarKey, PersistentDataType.INTEGER, i))
                        .build()));
                return;
            }

            this.gui.addItem(new GuiItem(ItemBuilder.from(Material.ORANGE_STAINED_GLASS_PANE)
                    .name(Component.text("Türchen " + i + " bereits geöffnet", NamedTextColor.GOLD))
                    .persistentDataContainer(pdc -> pdc.set(this.winterVillage.calendarKey, PersistentDataType.INTEGER, i))
                    .build()));
        });
    }

    public Gui getGui() {
        return this.gui;
    }
}
