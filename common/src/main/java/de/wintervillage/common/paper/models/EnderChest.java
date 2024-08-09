package de.wintervillage.common.paper.models;

import org.bson.Document;
import org.bson.types.Binary;

import java.util.HashMap;

public record EnderChest(HashMap<Integer, Item> enderChestItems) {

    public static EnderChest generate(Document document) {
        HashMap<Integer, Item> items = new HashMap<>();
        for (var entry : document.entrySet()) {
            int slot = Integer.parseInt(entry.getKey());
            byte[] bytes = ((Binary) entry.getValue()).getData();

            items.put(slot, new Item(bytes));
        }
        return new EnderChest(items);
    }

    public static EnderChest generateDefault() {
        return new EnderChest(new HashMap<>());
    }

    @Override
    public String toString() {
        return "EnderChest{" +
                "enderChestItems=" + this.enderChestItems +
                '}';
    }
}
