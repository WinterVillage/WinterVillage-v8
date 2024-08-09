package de.wintervillage.common.core.player.data;

import com.google.common.collect.Maps;
import de.wintervillage.common.paper.models.*;
import org.bson.Document;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerInformation {

    private Inventory inventory;
    private EnderChest enderChest;
    private PotionEffects potionEffects;
    private Advancements advancements;

    public PlayerInformation(
            Inventory inventory,
            EnderChest enderChest,
            PotionEffects potionEffects,
            Advancements advancements
    ) {
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.potionEffects = potionEffects;
        this.advancements = advancements;
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

    public Advancements advancements() {
        return this.advancements;
    }

    public void advancements(Advancements advancements) {
        this.advancements = advancements;
    }

    /**
     * Clears the information
     */
    public void clear() {
        this.inventory(Inventory.generateDefault());
        this.enderChest(EnderChest.generateDefault());
        this.potionEffects(PotionEffects.generateDefault());
        this.advancements(Advancements.generateDefault());
    }

    /**
     * Saves the information
     *
     * @param player {@link Player} the player to save
     */
    public void save(Player player) {
        this.clear();

        // player inventory
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack == null) continue;

            this.inventory.inventoryItems().put(i, new Item(itemStack.serializeAsBytes()));
        }

        // enderchest
        for (int i = 0; i < player.getEnderChest().getSize(); i++) {
            ItemStack itemStack = player.getEnderChest().getItem(i);
            if (itemStack == null) continue;

            this.enderChest.enderChestItems().put(i, new Item(itemStack.serializeAsBytes()));
        }

        // potion effects
        player.getActivePotionEffects().forEach(potionEffect -> {
            this.potionEffects.effects().add(new Effect(
                    potionEffect.getType().getKey().toString(),
                    potionEffect.getDuration(),
                    potionEffect.getAmplifier(),
                    potionEffect.isAmbient(),
                    potionEffect.hasParticles(),
                    potionEffect.hasIcon()
            ));
        });

        // advancements
        Registry.ADVANCEMENT.forEach(advancement -> {
            final AdvancementProgress progress = player.getAdvancementProgress(advancement);
            final Map<String, Date> awardedCritera = Maps.newHashMap();

            progress.getAwardedCriteria().forEach(key -> awardedCritera.put(key, progress.getDateAwarded(key)));

            if (awardedCritera.isEmpty()) return;
            this.advancements.advancements().add(new Advancement(
                    advancement.getKey().getKey(),
                    awardedCritera.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTime()))
            ));
        });
    }


    /**
     * Applies the information
     *
     * @param player {@link Player} the player to apply the information to
     */
    public void apply(Player player) {
        // player inventory
        for (var entry : this.inventory.inventoryItems().entrySet()) {
            player.getInventory().setItem(entry.getKey(), ItemStack.deserializeBytes(entry.getValue().bytes()));
        }

        // enderchest
        for (var entry : this.enderChest.enderChestItems().entrySet()) {
            player.getEnderChest().setItem(entry.getKey(), ItemStack.deserializeBytes(entry.getValue().bytes()));
        }

        // potion effects
        this.potionEffects.effects().stream().map(effect -> new PotionEffect(
                Registry.POTION_EFFECT_TYPE.get(NamespacedKey.fromString(effect.key().split(":")[1])),
                effect.duration(),
                effect.amplifier(),
                effect.ambient(),
                effect.particles(),
                effect.icon()
        )).forEach(player::addPotionEffect);

        // advancements
        Registry.ADVANCEMENT.forEach(advancement -> {
            final AdvancementProgress progress = player.getAdvancementProgress(advancement);
            final Optional<Advancement> optional = this.advancements.advancements().stream()
                    .filter(a -> a.key().equals(advancement.getKey().getKey()))
                    .findFirst();
            if (optional.isEmpty()) {
                this.setAdvancement(advancement, player, List.of(), progress.getAwardedCriteria());
                return;
            }

            final Map<String, Date> criteria = optional.get().completedCriteria()
                    .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new Date(entry.getValue())));
            this.setAdvancement(
                    advancement,
                    player,
                    criteria.keySet().stream().filter(key -> !progress.getAwardedCriteria().contains(key)).toList(),
                    progress.getAwardedCriteria().stream().filter(key -> !criteria.containsKey(key)).toList()
            );
        });
    }

    private void setAdvancement(
            org.bukkit.advancement.Advancement advancement,
            Player player,
            Collection<String> toAward,
            Collection<String> toRevoke
    ) {
        final AdvancementProgress progress = player.getAdvancementProgress(advancement);
        toAward.forEach(progress::awardCriteria);
        toRevoke.forEach(progress::revokeCriteria);
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
            Document effectDocument = new Document("key", effect.key())
                    .append("duration", effect.duration())
                    .append("amplifier", effect.amplifier())
                    .append("ambient", effect.ambient())
                    .append("particles", effect.particles())
                    .append("icon", effect.icon());
            potionEffectsDocument.put(effect.key(), effectDocument);
        }

        // advancements
        Document advancementsDocument = new Document();
        for (var advancement : playerInformation.advancements().advancements()) {
            Document advancementDocument = new Document("key", advancement.key())
                    .append("completedCriteria", advancement.completedCriteria().entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            advancementsDocument.put(advancement.key(), advancementDocument);
        }

        document.append("inventory", inventoryDocument)
                .append("enderchest", enderChestDocument)
                .append("potionEffects", potionEffectsDocument)
                .append("advancements", advancementsDocument);
        return document;
    }

    public static PlayerInformation fromDocument(Document document) {
        Inventory inventory = document.get("inventory", Document.class) != null && !document.get("inventory", Document.class).isEmpty()
                ? Inventory.generate(document.get("inventory", Document.class))
                : Inventory.generateDefault();

        EnderChest enderChest = document.get("enderchest", Document.class) != null && !document.get("enderchest", Document.class).isEmpty()
                ? EnderChest.generate(document.get("enderchest", Document.class))
                : EnderChest.generateDefault();

        PotionEffects potionEffects = document.containsKey("potionEffects") && !document.get("potionEffects", Document.class).isEmpty()
                ? PotionEffects.generate(document.get("potionEffects", Document.class))
                : PotionEffects.generateDefault();

        Advancements advancements = document.containsKey("advancements") && !document.get("advancements", Document.class).isEmpty()
                ? Advancements.generate(document.get("advancements", Document.class))
                : Advancements.generateDefault();

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

        // advancements
        Collection<Advancement> advancements = advancementsDocument.entrySet().stream().map(entry -> {
            Document advancementDocument = (Document) entry.getValue();
            return new Advancement(
                    advancementDocument.getString("key"),
                    advancementDocument.get("completedCriteria", Document.class).entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, criteraValue -> (Long) criteraValue.getValue()))
            );
        }).collect(Collectors.toList());

        return new PlayerInformation(
                inventory,
                enderChest,
                potionEffects,
                advancements
        );
    }

    @Override
    public String toString() {
        return "PlayerInformation{" +
                "inventory=" + this.inventory +
                ", enderChest=" + this.enderChest +
                ", potionEffects=" + this.potionEffects +
                ", advancements=" + this.advancements +
                '}';
    }
}
