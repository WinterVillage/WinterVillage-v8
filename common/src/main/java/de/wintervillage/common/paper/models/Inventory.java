package de.wintervillage.common.paper.models;

import org.bson.Document;
import org.bson.types.Binary;

import java.util.HashMap;

public record Inventory(HashMap<Integer, Item> inventoryItems) {

    public static Inventory generate(Document document) {
        HashMap<Integer, Item> items = new HashMap<>();
        for (var entry : document.entrySet()) {
            int slot = Integer.parseInt(entry.getKey());
            byte[] bytes = ((Binary) entry.getValue()).getData();

            items.put(slot, new Item(bytes));
        }
        return new Inventory(items);
    }

    public static Inventory generateDefault() {
        return new Inventory(new HashMap<>());
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryItems=" + this.inventoryItems +
                '}';
    }
}
