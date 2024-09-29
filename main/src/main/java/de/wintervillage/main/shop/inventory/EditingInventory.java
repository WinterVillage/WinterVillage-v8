package de.wintervillage.main.shop.inventory;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.CustomGuiItem;
import de.wintervillage.main.shop.Shop;
import dev.triumphteam.gui.components.util.ItemNbt;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;

import static de.wintervillage.common.paper.util.InventoryModifications.*;

public class EditingInventory {

    private final WinterVillage winterVillage;
    private final @NotNull Shop shop;

    private final Gui gui;

    public EditingInventory(@NotNull Shop shop) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.shop = shop;

        this.gui = Gui.gui()
                .title(Component.text("Befülle deinen Shop", NamedTextColor.BLUE).decorate(TextDecoration.BOLD))
                .rows(6)
                .create();

        this.gui.setDefaultClickAction(event -> {
            if (swapsWrongItem(event, this.shop.item())
                    || placesWrongItem(event, this.shop.item())
                    || dropsWrongItem(event, this.shop.item())
                    || otherWrongEvent(event, this.shop.item()))
                event.setResult(Event.Result.DENY);
        });

        this.gui.setDragAction(event -> {
            if (dragsWrong(event, this.shop.item())) event.setResult(Event.Result.DENY);
        });

        this.gui.setCloseGuiAction(event -> {
            int newAmount = Arrays.stream(this.gui.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .map(itemStack -> ItemNbt.removeTag(itemStack.clone(), "mf-gui"))
                    .filter(cleaned -> this.shop.item().isSimilar(cleaned))
                    .mapToInt(ItemStack::getAmount)
                    .sum();

            this.winterVillage.shopDatabase.modify(this.shop.uniqueId(), builder -> builder.amount(BigDecimal.valueOf(newAmount)))
                    .thenAccept(updatedShop -> {
                        Bukkit.getScheduler().runTask(this.winterVillage, () -> {
                            this.shop.amount(updatedShop.amount());
                            this.shop.updateInformation();
                        });

                        event.getPlayer().sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.shop.updated-shop")
                        ));
                    })
                    .exceptionally(throwable -> {
                        event.getPlayer().sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.shop.updating-failed",
                                        Component.text(throwable.getMessage())
                                )
                        ));
                        return null;
                    });
        });

        if (shop.item() == null) return;

        int amount = shop.amount().setScale(0, RoundingMode.DOWN).intValueExact();

        while (amount > 0) {
            int stackAmount = Math.min(amount, shop.item().getMaxStackSize());

            ItemStack itemStack = shop.item().clone();
            itemStack.setAmount(stackAmount);

            this.gui.addItem(new CustomGuiItem(itemStack));

            amount -= stackAmount;
        }
    }

    public Gui getGui() {
        return this.gui;
    }
}
