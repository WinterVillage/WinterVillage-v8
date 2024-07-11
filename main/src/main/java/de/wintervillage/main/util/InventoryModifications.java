package de.wintervillage.main.util;

import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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

        return action == InventoryAction.MOVE_TO_OTHER_INVENTORY || isTakeAction(action);
    }

    /**
     * Checks if the event is a place item action in the inventory
     *
     * @param event {@link InventoryClickEvent} to check
     * @return True if the action is placing an item in the inventory
     */
    public static boolean isPlacingItem(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "InventoryClickEvent cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        // shift click on item in players inventory
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER
                && inventory.getType() != clickedInventory.getType()) return true;

        return isPlaceAction(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER;
    }

    /**
     * Checks if the event is a swap item action in the inventory
     *
     * @param event {@link InventoryClickEvent} to check
     * @return True if the action is swapping an item in the inventory
     */
    public static boolean isSwappingItem(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "InventoryClickEvent cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        return isSwapAction(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER;
    }

    /**
     * Checks if the event is a drop item action in the inventory
     *
     * @param event {@link InventoryClickEvent} to check
     * @return True if the action is dropping an item in the inventory
     */
    public static boolean isDroppingItem(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "InventoryClickEvent cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        return isDropAction(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
    }

    /**
     * Checks if the event is an undefined or other type of action in the inventory
     *
     * @param event {@link InventoryClickEvent} to check
     * @return True if the action is undefined or another type of action
     */
    public static boolean isOtherEvent(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "InventoryClickEvent cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        return isMiscAction(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
    }

    /**
     * Checks if any item is being dragged from the inventory
     *
     * @param event {@link InventoryDragEvent} to check
     * @return True if any item is being dragged from the inventory
     */
    public static boolean isDraggingItem(final InventoryDragEvent event) {
        Preconditions.checkNotNull(event, "InventoryDragEvent cannot be null");
        final int topSlots = event.getView().getTopInventory().getSize();
        // is dragging on any top inventory slot
        return event.getRawSlots().stream().anyMatch(slot -> slot < topSlots);
    }

    /**
     * Checks if the action is a take item action
     *
     * @param action {@link InventoryAction} to check
     * @return True if the action is a take item action
     */
    private static boolean isTakeAction(final InventoryAction action) {
        Preconditions.checkNotNull(action, "InventoryAction cannot be null");
        return ITEM_TAKE_ACTIONS.contains(action);
    }

    /**
     * Checks if the action is a place item action
     *
     * @param action {@link InventoryAction} to check
     * @return True if the action is a place item action
     */
    private static boolean isPlaceAction(final InventoryAction action) {
        Preconditions.checkNotNull(action, "InventoryAction cannot be null");
        return ITEM_PLACE_ACTIONS.contains(action);
    }

    /**
     * Checks if the action is a swap item action
     *
     * @param action {@link InventoryAction} to check
     * @return True if the action is a swap item action
     */
    private static boolean isSwapAction(final InventoryAction action) {
        Preconditions.checkNotNull(action, "InventoryAction cannot be null");
        return ITEM_SWAP_ACTIONS.contains(action);
    }

    /**
     * Checks if the action is a drop item action
     *
     * @param action {@link InventoryAction} to check
     * @return True if the action is a drop item action
     */
    private static boolean isDropAction(final InventoryAction action) {
        Preconditions.checkNotNull(action, "InventoryAction cannot be null");
        return ITEM_DROP_ACTIONS.contains(action);
    }

    /**
     * Checks if the action is a miscellaneous action
     *
     * @param action {@link InventoryAction} to check
     * @return True if the action is a miscellaneous action
     */
    private static boolean isMiscAction(final InventoryAction action) {
        Preconditions.checkNotNull(action, "InventoryAction cannot be null");
        return action == InventoryAction.CLONE_STACK || action == InventoryAction.UNKNOWN;
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
            InventoryAction.SWAP_WITH_CURSOR
            // InventoryAction.HOTBAR_MOVE_AND_READD moved to HOTBAR_SWAP
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
