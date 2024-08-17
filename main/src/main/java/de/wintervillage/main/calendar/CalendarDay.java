package de.wintervillage.main.calendar;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface CalendarDay {

    /**
     * The day of the calendar
     * @return {@link Integer} day
     */
    int day();

    /**
     * The {@link ItemStack} of the calendar day
     * @return {@link ItemStack} itemStack
     */
    ItemStack itemStack();

    /**
     * The list of {@link UUID} of players who have opened the calendar day
     * @return {@link List} of {@link UUID}
     */
    List<UUID> opened();

    /**
     * Add a player to the list of players who have opened the calendar day
     * @param uuid {@link UUID} of the player
     */
    void addOpened(UUID uuid);
}
