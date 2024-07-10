package de.wintervillage.main.calendar;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.IntStream;

public class CalendarInventory implements InventoryHolder {

    private final WinterVillage winterVillage;
    private final Inventory inventory;

    public CalendarInventory(Player player) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.inventory = this.winterVillage.getServer().createInventory(
                this,
                54,
                Component.text("Adventskalender", NamedTextColor.RED).decoration(TextDecoration.BOLD, true)
        );

        this.fillBorder(ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).build());

        int current = LocalDate.now().getDayOfMonth();
        IntStream.rangeClosed(1, 24).forEach(i -> {
            ItemStack itemStack = ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                    .amount(1)
                    .persistentDataContainer(pdc -> pdc.set(this.winterVillage.calendarKey, PersistentDataType.INTEGER, i))
                    .build();

            Optional<CalendarDay> optional = this.winterVillage.calendarHandler.byDay(i);
            if (!optional.isPresent()) {
                this.inventory.addItem(ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                        .name(Component.text("Türchen " + i + " verschlossen", NamedTextColor.RED))
                        .persistentDataContainer(pdc -> pdc.set(this.winterVillage.calendarKey, PersistentDataType.INTEGER, i))
                        .build());
                return;
            }

            if (current < i) {
                this.inventory.addItem(ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                        .name(Component.text("Türchen " + i + " verschlossen", NamedTextColor.RED))
                        .persistentDataContainer(pdc -> pdc.set(this.winterVillage.calendarKey, PersistentDataType.INTEGER, i))
                        .build());
                return;
            }

            if (!optional.get().getOpened().contains(player.getUniqueId())) {
                this.inventory.addItem(ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                        .name(Component.text("Türchen " + i + " öffnen", NamedTextColor.GREEN))
                        .persistentDataContainer(pdc -> pdc.set(this.winterVillage.calendarKey, PersistentDataType.INTEGER, i))
                        .build());
                return;
            }

            this.inventory.addItem(ItemBuilder.from(Material.ORANGE_STAINED_GLASS_PANE)
                    .name(Component.text("Türchen " + i + " bereits geöffnet", NamedTextColor.GOLD))
                    .persistentDataContainer(pdc -> pdc.set(this.winterVillage.calendarKey, PersistentDataType.INTEGER, i))
                    .build());
        });
    }

    private void fillBorder(ItemStack itemStack) {
        if (this.inventory == null) return;

        int size = this.inventory.getSize();
        int rows = size / 9;

        // TOP | BOTTOM
        for (int i = 0; i < 9; i++) {
            this.inventory.setItem(i, itemStack);
            this.inventory.setItem(size - 9 + i, itemStack);
        }

        // LEFT | RIGHT
        for (int i = 1; i < rows - 1; i++) {
            this.inventory.setItem(i * 9, itemStack);
            this.inventory.setItem(i * 9 + 8, itemStack);
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
