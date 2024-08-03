package de.wintervillage.common.core.player.data;

import org.bson.Document;
import org.bson.types.Binary;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;

public class PlayerInformation {

    private Inventory inventory;
    private EnderChest enderChest;

    public PlayerInformation(
            Inventory inventory,
            EnderChest enderChest
    ) {
        this.inventory = inventory;
        this.enderChest = enderChest;
    }

    public Inventory inventory() {
        return this.inventory;
    }

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public EnderChest enderChest() {
        return this.enderChest;
    }

    public void enderChest(EnderChest enderChest) {
        this.enderChest = enderChest;
    }

    /**
     * Clears the {@link PlayerInformation}
     */
    public void clear() {
        this.inventory.inventoryItems().clear();
        this.enderChest.enderChestItems().clear();
    }

    /**
     * Saves the {@link PlayerInformation}
     * @param player the player to save
     */
    public void save(Player player) {
        this.clear();

        // player inventory
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack == null) continue;

            this.inventory.inventoryItems.put(i, new PlayerInformation.Item(itemStack.serializeAsBytes()));
        }

        // enderchest
        for (int i = 0; i < player.getEnderChest().getSize(); i++) {
            ItemStack itemStack = player.getEnderChest().getItem(i);
            if (itemStack == null) continue;

            this.enderChest.enderChestItems.put(i, new PlayerInformation.Item(itemStack.serializeAsBytes()));
        }
    }


    /**
     * Applies the {@link PlayerInformation}
     * @param player the player to apply the information to
     */
    public void apply(Player player) {
        // player inventory
        for (var entry : this.inventory.inventoryItems.entrySet()) {
            player.getInventory().setItem(entry.getKey(), ItemStack.deserializeBytes(entry.getValue().bytes()));
        }

        // enderchest
        for (var entry : this.enderChest.enderChestItems.entrySet()) {
            player.getEnderChest().setItem(entry.getKey(), ItemStack.deserializeBytes(entry.getValue().bytes()));
        }
    }

    public Document toDocument(PlayerInformation playerInformation) {
        Document document = new Document();

        // player information
        Document inventoryDocument = new Document();
        for (var entry : playerInformation.inventory().inventoryItems().entrySet()) {
            inventoryDocument.put(entry.getKey().toString(), entry.getValue().bytes());
        }

        // enderchest
        Document enderChestDocument = new Document();
        for (var entry : playerInformation.enderChest().enderChestItems().entrySet()) {
            enderChestDocument.put(entry.getKey().toString(), entry.getValue().bytes());
        }

        document.put("inventory", inventoryDocument);
        document.put("enderchest", enderChestDocument);
        return document;
    }

    public static PlayerInformation fromDocument(Document document) {
        Document inventoryDocument = document.get("inventory", Document.class);
        Document enderchestDocument = document.get("enderchest", Document.class);

        // player inventory
        HashMap<Integer, Item> inventoryItems = new HashMap<>();
        for (var entry : inventoryDocument.entrySet()) {
            int slot = Integer.parseInt(entry.getKey());
            byte[] bytes = ((Binary) entry.getValue()).getData();

            inventoryItems.put(slot, new Item(bytes));
        }

        // enderchest
        HashMap<Integer, Item> enderChestItems = new HashMap<>();
        for (var entry : enderchestDocument.entrySet()) {
            int slot = Integer.parseInt(entry.getKey());
            byte[] bytes = ((Binary) entry.getValue()).getData();

            enderChestItems.put(slot, new Item(bytes));
        }

        return new PlayerInformation(
                new Inventory(inventoryItems),
                new EnderChest(enderChestItems)
        );
    }

    @Override
    public String toString() {
        return "PlayerInformation{" +
                "inventory=" + inventory +
                ", enderChest=" + enderChest +
                '}';
    }

    public record Item(byte[] bytes) {

        @Override
        public String toString() {
            return "Item{" +
                    "bytes=" + Arrays.toString(bytes) +
                    '}';
        }
    }

    public record Inventory(HashMap<Integer, Item> inventoryItems) {

        @Override
        public String toString() {
            return "Inventory{" +
                    "inventoryItems=" + this.inventoryItems +
                    '}';
        }
    }

    public record EnderChest(HashMap<Integer, Item> enderChestItems) {

        @Override
        public String toString() {
            return "EnderChest{" +
                    "enderChestItems=" + this.enderChestItems +
                    '}';
        }
    }
}
