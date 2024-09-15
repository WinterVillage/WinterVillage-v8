package de.wintervillage.common.core.player.data;

import com.google.common.collect.Maps;
import de.wintervillage.common.paper.models.*;
import de.wintervillage.common.paper.util.KeyedAdapter;
import org.bson.Document;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerInformation {

    private Inventory inventory;
    private EnderChest enderChest;
    private PotionEffects potionEffects;
    private Advancements advancements;
    private Generic generic;
    private Statistics statistics;

    public PlayerInformation() {
        this(
                Inventory.generateDefault(),
                EnderChest.generateDefault(),
                PotionEffects.generateDefault(),
                Advancements.generateDefault(),
                Generic.generateDefault(),
                Statistics.generateDefault()
        );
    }

    public PlayerInformation(
            Inventory inventory,
            EnderChest enderChest,
            PotionEffects potionEffects,
            Advancements advancements,
            Generic generic,
            Statistics statistics
    ) {
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.potionEffects = potionEffects;
        this.advancements = advancements;
        this.generic = generic;
        this.statistics = statistics;
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

    public Generic generic() {
        return this.generic;
    }

    public void generic(Generic generic) {
        this.generic = generic;
    }

    public Statistics statistics() {
        return this.statistics;
    }

    public void statistics(Statistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Clears the information
     */
    public void clear() {
        this.inventory(Inventory.generateDefault());
        this.enderChest(EnderChest.generateDefault());
        this.potionEffects(PotionEffects.generateDefault());
        this.advancements(Advancements.generateDefault());
        this.generic(Generic.generateDefault());
        this.statistics(Statistics.generateDefault());
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

        // generic
        this.generic = new Generic(
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
                player.getHealth(),
                player.getFoodLevel(),
                player.getExhaustion(),
                player.getSaturation(),
                player.getAllowFlight(),
                player.isFlying(),
                player.getGameMode().name(),
                player.getFireTicks(),
                player.getExp(),
                player.getLevel()
        );

        // statistics
        Registry.STATISTIC.forEach(statistic -> {
            switch (statistic.getType()) {
                case UNTYPED -> this.addStatistic(player, statistic, this.statistics.generic());
                case BLOCK -> this.addMaterialStatistic(player, statistic, this.statistics.blocks(), true);
                case ITEM -> this.addMaterialStatistic(player, statistic, this.statistics.items(), false);
                case ENTITY -> this.addEntityStatistic(player, statistic, this.statistics.entities());
            }
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

        // generic
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.generic.maxHealth());
        player.setHealth(this.generic.health());
        player.setFoodLevel(this.generic.foodLevel());
        player.setExhaustion(this.generic.exhaustion());
        player.setSaturation(this.generic.saturation());
        player.setAllowFlight(this.generic.allowFlight());
        player.setFlying(this.generic.isFlying());
        player.setGameMode(GameMode.valueOf(this.generic.gameMode()));
        player.setFireTicks(this.generic.fireTicks());
        player.setExp(this.generic.experience());
        player.setLevel(this.generic.level());

        // statistics
        this.statistics().generic().forEach((id, value) -> this.applyStatistic(player, id, Statistic.Type.UNTYPED, value));
        this.statistics().blocks().forEach((id, map) -> map.forEach((key, value) -> this.applyStatistic(player, id, Statistic.Type.BLOCK, value, key)));
        this.statistics().items().forEach((id, map) -> map.forEach((key, value) -> this.applyStatistic(player, id, Statistic.Type.ITEM, value, key)));
        this.statistics().entities().forEach((id, map) -> map.forEach((key, value) -> this.applyStatistic(player, id, Statistic.Type.ENTITY, value, key)));
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

    private void applyStatistic(@NotNull Player player, @NotNull String id, @NotNull Statistic.Type type, int value, @NotNull String... key) {
        final Statistic statistic = KeyedAdapter.matchStatistic(id);
        if (statistic == null) return;

        switch (type) {
            case UNTYPED -> player.setStatistic(statistic, value);
            case BLOCK, ITEM -> player.setStatistic(statistic, KeyedAdapter.matchMaterial(key[0]), value);
            case ENTITY -> player.setStatistic(statistic, KeyedAdapter.matchEntityType(key[0]), value);
        }
    }

    private void addStatistic(Player player, Statistic statistic, Map<String, Integer> map) {
        final int value = player.getStatistic(statistic);
        if (value != 0) map.put(statistic.getKey().getKey(), value);
    }

    private void addMaterialStatistic(Player player, Statistic statistic, Map<String, Map<String, Integer>> map, boolean isBlock) {
        Registry.MATERIAL.forEach(material -> {
            if ((material.isBlock() && !isBlock) || (material.isItem() && isBlock)) return;
            final int value = player.getStatistic(statistic, material);
            if (value != 0) map.computeIfAbsent(statistic.getKey().getKey(), k -> Maps.newHashMap()).put(material.getKey().getKey(), value);
        });
    }

    private void addEntityStatistic(Player player, Statistic statistic, Map<String, Map<String, Integer>> map) {
        Registry.ENTITY_TYPE.forEach(entityType -> {
            if (!entityType.isAlive()) return;
            final int value = player.getStatistic(statistic, entityType);
            if (value != 0) map.computeIfAbsent(statistic.getKey().getKey(), key -> Maps.newHashMap()).put(entityType.getKey().getKey(), value);
        });
    }

    public Document toDocument() {
        return new Document("inventory", this.inventory().document())
                .append("enderchest", this.enderChest().document())
                .append("potionEffects", this.potionEffects().document())
                .append("advancements", this.advancements().document())
                .append("generic", this.generic().document())
                .append("statistics", this.statistics().document());
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

        Generic generic = document.containsKey("generic") && !document.get("generic", Document.class).isEmpty()
                ? Generic.generate(document.get("generic", Document.class))
                : Generic.generateDefault();

        Statistics statistics = document.containsKey("statistics") && !document.get("statistics", Document.class).isEmpty()
                ? Statistics.generate(document.get("statistics", Document.class))
                : Statistics.generateDefault();

        return new PlayerInformation(
                inventory,
                enderChest,
                potionEffects,
                advancements,
                generic,
                statistics
        );
    }

    @Override
    public String toString() {
        return "PlayerInformation{" +
                "inventory=" + this.inventory +
                ", enderChest=" + this.enderChest +
                ", potionEffects=" + this.potionEffects +
                ", advancements=" + this.advancements +
                ", generic=" + this.generic +
                ", statistics=" + this.statistics +
                '}';
    }
}
