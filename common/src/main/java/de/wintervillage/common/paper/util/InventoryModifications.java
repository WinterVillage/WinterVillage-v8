package de.wintervillage.common.paper.util;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @see <a href="https://github.com/TriumphTeam/triumph-gui/blob/master/core/src/main/java/dev/triumphteam/gui/guis/InteractionModifierListener.java">Find class on GitHub</a>
 */
public final class InventoryModifications {

    /**
     * Checks if the event is a take item action from the inventory
     *
     * @param event {@link InventoryClickEvent} to check
     * @return True if the action is taking an item from the inventory
     */
    public static boolean isTakingItem(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "InventoryClickEvent cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        if (clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER || inventory.getType() == InventoryType.PLAYER)
            return false;

        return action == InventoryAction.MOVE_TO_OTHER_INVENTORY || ITEM_TAKE_ACTIONS.contains(action);
    }

    private static boolean isPlacingItem(final InventoryClickEvent event, final ItemStack itemStack, boolean compare) {
        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        // shift click on item in player inventory
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER
                && inventory.getType() != clickedInventory.getType()) {
            ItemStack moving = event.getCurrentItem();
            if (moving == null || (compare && !moving.isSimilar(itemStack))) // check type?
                return true;
        }

        // normal click on gui empty slot with item on cursor
        if (ITEM_PLACE_ACTIONS.contains(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER) {
            ItemStack cursor = event.getCursor();
            return !compare || (cursor != null && !cursor.isSimilar(itemStack));
        }

        return false;
    }

    public static boolean placesWrongItem(final InventoryClickEvent event, final NamespacedKey key) {
        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        // shift click on item in player inventory
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER
                && inventory.getType() != clickedInventory.getType()) {
            ItemStack moving = event.getCurrentItem();
            if (moving == null || hasKey(moving, key))
                return true;
        }

        // normal click on gui empty slot with item on cursor
        if (ITEM_PLACE_ACTIONS.contains(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER) {
            ItemStack cursor = event.getCursor();
            return (hasKey(cursor, key));
        }

        return false;
    }

    public static boolean placesWrongItem(final InventoryClickEvent event, final ItemStack itemStack) {
        return isPlacingItem(event, itemStack, true);
    }

    public static boolean placesWrongItem(final InventoryClickEvent event) {
        return isPlacingItem(event, null, false);
    }

    private static boolean isSwappingItem(final InventoryClickEvent event, final ItemStack itemStack, boolean compare) {
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

        return !compare || !hotBarItem.isSimilar(itemStack);
    }

    public static boolean swapsWrongItem(final InventoryClickEvent event, final NamespacedKey key) {
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

        return hasKey(hotBarItem, key);
    }

    public static boolean swapsWrongItem(final InventoryClickEvent event, final ItemStack itemStack) {
        return isSwappingItem(event, itemStack, true);
    }

    public static boolean swapsWrongItem(final InventoryClickEvent event) {
        return isSwappingItem(event, null, false);
    }

    private static boolean isDroppingItem(final InventoryClickEvent event, final ItemStack itemStack, boolean compare) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        boolean isDropping = ITEM_DROP_ACTIONS.contains(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
        if (!isDropping) return false;

        ItemStack droppedItem = event.getCurrentItem();
        if (droppedItem == null) return true;

        return !compare || !droppedItem.isSimilar(itemStack);
    }

    public static boolean dropsWrongItem(final InventoryClickEvent event,final NamespacedKey key) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        boolean isDropping = ITEM_DROP_ACTIONS.contains(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
        if (!isDropping) return false;

        ItemStack droppedItem = event.getCurrentItem();
        if (droppedItem == null) return true;

        return hasKey(droppedItem, key);
    }

    public static boolean dropsWrongItem(final InventoryClickEvent event, final ItemStack itemStack) {
        return isDroppingItem(event, itemStack, true);
    }

    public static boolean dropsWrongItem(final InventoryClickEvent event) {
        return isDroppingItem(event, null, false);
    }

    public static boolean otherWrongEvent(final InventoryClickEvent event, final NamespacedKey key) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        boolean isOther = (action == InventoryAction.CLONE_STACK || action == InventoryAction.UNKNOWN)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
        if (!isOther) return false;

        ItemStack involvedItem = event.getCurrentItem();
        if (involvedItem == null) return true;

        return hasKey(involvedItem, key);
    }

    private static boolean otherWrongEvent(final InventoryClickEvent event, final ItemStack itemStack, boolean compare) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        boolean isOther = (action == InventoryAction.CLONE_STACK || action == InventoryAction.UNKNOWN)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
        if (!isOther) return false;

        ItemStack involvedItem = event.getCurrentItem();
        if (involvedItem == null) return true;

        return !compare || !involvedItem.isSimilar(itemStack);
    }

    public static boolean otherWrongEvent(final InventoryClickEvent event, final ItemStack itemStack) {
        return otherWrongEvent(event, itemStack, true);
    }

    public static boolean otherWrongEvent(final InventoryClickEvent event) {
        return otherWrongEvent(event, null, false);
    }

    private static boolean isDraggingItem(final InventoryDragEvent event, final ItemStack itemStack, boolean compare) {
        final int topSlots = event.getView().getTopInventory().getSize();
        ItemStack draggedItem = event.getOldCursor();

        boolean isDraggingInTopInventory = event.getRawSlots().stream().anyMatch(slot -> slot < topSlots);
        if (!isDraggingInTopInventory) return false;

        return !compare || draggedItem == null || !draggedItem.isSimilar(itemStack);
    }

    public static boolean dragsWrong(final InventoryDragEvent event, final NamespacedKey key) {
        final int topSlots = event.getView().getTopInventory().getSize();
        ItemStack draggedItem = event.getOldCursor();

        boolean isDraggingInTopInventory = event.getRawSlots().stream().anyMatch(slot -> slot < topSlots);
        if (!isDraggingInTopInventory) return false;

        return hasKey(draggedItem, key);
    }

    public static boolean dragsWrong(final InventoryDragEvent event, final ItemStack itemStack) {
        return isDraggingItem(event, itemStack, true);
    }

    public static boolean dragsWrong(final InventoryDragEvent event) {
        return isDraggingItem(event, null, false);
    }

    private static boolean hasKey(final ItemStack itemStack, final NamespacedKey key) {
        if (itemStack == null || itemStack.getType() == Material.AIR || key == null) return false;

        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.has(key);
    }

    /**
     * Holds all the actions that should be considered "take" actions
     */
    private static final Set<InventoryAction> ITEM_TAKE_ACTIONS = Collections.unmodifiableSet(EnumSet.of(
            InventoryAction.PICKUP_ONE,
            InventoryAction.PICKUP_SOME,
            InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_ALL,
            InventoryAction.COLLECT_TO_CURSOR,
            InventoryAction.HOTBAR_SWAP,
            InventoryAction.MOVE_TO_OTHER_INVENTORY
    ));

    /**
     * Holds all the actions that should be considered "place" actions
     */
    private static final Set<InventoryAction> ITEM_PLACE_ACTIONS = Collections.unmodifiableSet(EnumSet.of(
            InventoryAction.PLACE_ONE,
            InventoryAction.PLACE_SOME,
            InventoryAction.PLACE_ALL
    ));

    /**
     * Holds all actions related to swapping items
     */
    private static final Set<InventoryAction> ITEM_SWAP_ACTIONS = Collections.unmodifiableSet(EnumSet.of(
            InventoryAction.HOTBAR_SWAP,
            InventoryAction.SWAP_WITH_CURSOR,
            InventoryAction.HOTBAR_MOVE_AND_READD // moved to HOTBAR_SWAP
    ));

    /**
     * Holds all actions related to dropping items
     */
    private static final Set<InventoryAction> ITEM_DROP_ACTIONS = Collections.unmodifiableSet(EnumSet.of(
            InventoryAction.DROP_ONE_SLOT,
            InventoryAction.DROP_ALL_SLOT,
            InventoryAction.DROP_ONE_CURSOR,
            InventoryAction.DROP_ALL_CURSOR
    ));
}
