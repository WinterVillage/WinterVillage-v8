package de.wintervillage.main.calendar.impl;

import de.wintervillage.main.calendar.CalendarDay;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.Binary;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CalendarDayImpl implements CalendarDay {

    @BsonProperty("day")
    private int day;

    @BsonProperty("item")
    private @NotNull ItemStack itemStack;

    @BsonProperty("opened")
    private @NotNull List<UUID> opened;

    public CalendarDayImpl(int day, @NotNull ItemStack itemStack, @NotNull List<UUID> opened) {
        this.day = day;
        this.itemStack = itemStack;
        this.opened = new ArrayList<>(opened);
    }

    @Override
    public int day() {
        return this.day;
    }

    @Override
    public ItemStack itemStack() {
        return this.itemStack;
    }

    @Override
    public List<UUID> opened() {
        return this.opened;
    }

    public void addOpened(UUID uuid) {
        this.opened.add(uuid);
    }

    public Document toDocument() {
        return new Document("day", this.day)
                .append("itemStack", this.itemStack.serializeAsBytes())
                .append("opened", this.opened.stream()
                        .map(UUID::toString)
                        .toList());
    }

    public static CalendarDayImpl fromDocument(Document document) {
        return new CalendarDayImpl(
                document.getInteger("day"),
                ItemStack.deserializeBytes(document.get("itemStack", Binary.class).getData()),
                new ArrayList<>(document.getList("opened", String.class)
                        .stream()
                        .map(UUID::fromString)
                        .toList())
        );
    }

    @Override
    public String toString() {
        return "CalendarDayImpl{" +
                "day=" + this.day +
                ", itemStack=" + this.itemStack +
                ", opened=" + this.opened +
                '}';
    }
}
