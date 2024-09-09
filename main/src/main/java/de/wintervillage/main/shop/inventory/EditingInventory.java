package de.wintervillage.main.shop.inventory;

import com.google.common.base.Preconditions;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import dev.triumphteam.gui.components.util.ItemNbt;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class EditingInventory {

    private final WinterVillage winterVillage;
    private final @NotNull Shop shop;

    private Gui gui;

    public EditingInventory(@NotNull Shop shop) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.shop = shop;

        this.gui = Gui.gui()
                .title(Component.text("Befülle deinen Shop", NamedTextColor.BLUE).decorate(TextDecoration.BOLD))
                .rows(6)
                .create();

        this.gui.setDefaultClickAction(event -> {
            if (this.swapsWrongItem(event)
                    || this.placesWrongItem(event)
                    || this.dropsWrongItem(event)
                    || this.otherWrongEvent(event))
                event.setResult(Event.Result.DENY);
        });

        this.gui.setDragAction(event -> {
            if (this.dragsWrong(event))
                event.setResult(Event.Result.DENY);
        });

        this.gui.setCloseGuiAction(event -> {
            int newAmount = Arrays.stream(this.gui.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .map(itemStack -> ItemNbt.removeTag(itemStack.clone(), "mf-gui"))
                    .filter(cleaned -> this.shop.item().isSimilar(cleaned))
                    .mapToInt(ItemStack::getAmount)
                    .sum();

            this.winterVillage.shopDatabase.modify(this.shop.uniqueId(), builder -> builder.amount(BigDecimal.valueOf(newAmount)))
                    .thenAccept(action -> {
                        Bukkit.getScheduler().runTask(this.winterVillage, () -> {
                            this.shop.amount(BigDecimal.valueOf(newAmount));
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

            this.gui.addItem(new GuiItem(itemStack));

            amount -= stackAmount;
        }
    }

    public Gui getGui() {
        return this.gui;
    }

    private boolean dragsWrong(final InventoryDragEvent event) {
        final int topSlots = event.getView().getTopInventory().getSize();
        ItemStack draggedItem = event.getOldCursor();

        boolean isDraggingInTopInventory = event.getRawSlots().stream().anyMatch(slot -> slot < topSlots);

        return isDraggingInTopInventory && (draggedItem == null || !draggedItem.getType().equals(this.shop.item().getType()));
    }

    private boolean otherWrongEvent(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        boolean isOther = (action == InventoryAction.CLONE_STACK || action == InventoryAction.UNKNOWN)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
        if (!isOther) return false;

        ItemStack involvedItem = event.getCurrentItem();
        return involvedItem == null || !involvedItem.getType().equals(this.shop.item().getType());
    }

    private boolean dropsWrongItem(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        boolean isDropping = ITEM_DROP_ACTIONS.contains(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
        if (!isDropping) return false;

        ItemStack droppedItem = event.getCurrentItem();
        return droppedItem == null || !droppedItem.getType().equals(this.shop.item().getType());
    }

    private boolean placesWrongItem(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        // shift click on item in player inventory
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER
                && inventory.getType() != clickedInventory.getType()) {
            ItemStack moving = event.getCurrentItem();
            if (moving == null || !moving.getType().equals(this.shop.item().getType()))
                return true;
        }

        // normal click on gui empty slot with item on cursor
        if (ITEM_PLACE_ACTIONS.contains(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER) {
            ItemStack cursor = event.getCursor();
            return !cursor.getType().equals(this.shop.item().getType());
        }

        return false;
    }

    private boolean swapsWrongItem(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        boolean isSwapping = ITEM_SWAP_ACTIONS.contains(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER;
        if (!isSwapping) return false;

        if (event.getHotbarButton() == -1) return false;
        ItemStack hotBarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
        if (hotBarItem == null || hotBarItem.getType() == Material.AIR) return false;

        return !hotBarItem.getType().equals(this.shop.item().getType());
    }

    /**
     * Holds all the actions that should be considered "place" actions
     */
    private static final Set<InventoryAction> ITEM_PLACE_ACTIONS = Collections.unmodifiableSet(EnumSet.of(InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ALL));

    /**
     * Holds all actions related to swapping items
     */
    private static final Set<InventoryAction> ITEM_SWAP_ACTIONS = Collections.unmodifiableSet(EnumSet.of(
            InventoryAction.HOTBAR_SWAP,
            InventoryAction.SWAP_WITH_CURSOR,
            InventoryAction.HOTBAR_MOVE_AND_READD // TODO: moved to HOTBAR_SWAP
    ));

    /**
     * Holds all actions relating to dropping items
     */
    private static final Set<InventoryAction> ITEM_DROP_ACTIONS = Collections.unmodifiableSet(EnumSet.of(InventoryAction.DROP_ONE_SLOT, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ALL_CURSOR));
}
