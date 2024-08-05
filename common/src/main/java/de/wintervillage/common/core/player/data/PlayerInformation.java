package de.wintervillage.common.core.player.data;

import org.bson.Document;
import org.bson.types.Binary;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerInformation {

    private Inventory inventory;
    private EnderChest enderChest;
    private PotionEffects potionEffects;

    public PlayerInformation(
            Inventory inventory,
            EnderChest enderChest,
            PotionEffects potionEffects
    ) {
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.potionEffects = potionEffects;
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

    public PotionEffects potionEffects() {
        return this.potionEffects;
    }

    public void potionEffects(PotionEffects potionEffects) {
        this.potionEffects = potionEffects;
    }

    /**
     * Clears the {@link PlayerInformation}
     */
    public void clear() {
        this.inventory.inventoryItems().clear();
        this.enderChest.enderChestItems().clear();
        this.potionEffects(new PotionEffects(new ArrayList<>()));
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

        // potion effects
        player.getActivePotionEffects().forEach(potionEffect -> {
            this.potionEffects.effects.add(new Effect(
                    potionEffect.getType().getKey().toString(),
                    potionEffect.getDuration(),
                    potionEffect.getAmplifier(),
                    potionEffect.isAmbient(),
                    potionEffect.hasParticles(),
                    potionEffect.hasIcon()
            ));
        });
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

        // potion effects
        this.potionEffects.effects.stream().map(effect -> new PotionEffect(
                Registry.POTION_EFFECT_TYPE.get(NamespacedKey.fromString(effect.key.split(":")[1])),
                effect.duration,
                effect.amplifier,
                effect.ambient,
                effect.particles,
                effect.icon
        )).forEach(player::addPotionEffect);
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

        // potion effects
        Document potionEffectsDocument = new Document();
        for (var effect : playerInformation.potionEffects().effects()) {
            Document effectDocument = new Document("key", effect.key)
                    .append("duration", effect.duration)
                    .append("amplifier", effect.amplifier)
                    .append("ambient", effect.ambient)
                    .append("particles", effect.particles)
                    .append("icon", effect.icon);
            potionEffectsDocument.put(effect.key, effectDocument);
        }

        document.append("inventory", inventoryDocument)
                .append("enderchest", enderChestDocument)
                .append("potionEffects", potionEffectsDocument);
        return document;
    }

    public static PlayerInformation fromDocument(Document document) {
        Document inventoryDocument = document.get("inventory", Document.class);
        Document enderchestDocument = document.get("enderchest", Document.class);
        Document potionEffectsDocument = document.get("potionEffects", Document.class);

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

        // potion effects
        Collection<Effect> effects = potionEffectsDocument.entrySet().stream().map(entry -> {
            Document effectDocument = (Document) entry.getValue();
            return new Effect(
                    effectDocument.getString("key"),
                    effectDocument.getInteger("duration"),
                    effectDocument.getInteger("amplifier"),
                    effectDocument.getBoolean("ambient"),
                    effectDocument.getBoolean("particles"),
                    effectDocument.getBoolean("icon")
            );
        }).collect(Collectors.toList());

        return new PlayerInformation(
                new Inventory(inventoryItems),
                new EnderChest(enderChestItems),
                new PotionEffects(effects)
        );
    }

    @Override
    public String toString() {
        return "PlayerInformation{" +
                "inventory=" + this.inventory +
                ", enderChest=" + this.enderChest +
                ", potionEffects=" + this.potionEffects +
                '}';
    }

    private record Item(byte[] bytes) {

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

    private record Effect(String key, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {

        @Override
        public String toString() {
            return "Effect{" +
                    "key='" + this.key + '\'' +
                    ", duration=" + this.duration +
                    ", amplifier=" + this.amplifier +
                    ", ambient=" + this.ambient +
                    ", particles=" + this.particles +
                    ", icon=" + this.icon +
                    '}';
        }
    }

    public record PotionEffects(Collection<Effect> effects) {

        public PotionEffects(Collection<Effect> effects) {
            this.effects = new ArrayList<>(effects);
        }

        @Override
        public String toString() {
            return "PotionEffects{" +
                    "effects=" + this.effects +
                    '}';
        }
    }
}
