package de.wintervillage.main.calendar;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CalendarDay {

    private int day;
    private ItemStack itemStack;

    private List<UUID> opened;

    /**
     * Default constructor for the CalendarDay class (used by MongoDB)
     */
    public CalendarDay() { }

    public CalendarDay(int day, ItemStack itemStack, List<UUID> opened) {
        this.day = day;
        this.itemStack = itemStack;
        this.opened = opened;
    }

    public int getDay() {
        return this.day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public List<UUID> getOpened() {
        return new ArrayList<>(this.opened);
    }

    public void setOpened(List<UUID> opened) {
        this.opened = opened;
    }

    @Override
    public String toString() {
        return "CalendarDay{" +
                "day=" + this.day +
                ", itemStack=" + this.itemStack +
                ", opened=" + this.opened +
                '}';
    }
}
