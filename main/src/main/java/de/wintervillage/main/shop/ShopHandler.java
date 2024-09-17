package de.wintervillage.main.shop;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.BiConsumer;

public class ShopHandler {

    private final WinterVillage winterVillage;

    private final List<Shop> shops;

    public final NamespacedKey shopKey, amountKey;

    @Inject
    public ShopHandler() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.shops = new ArrayList<>();

        this.shopKey = new NamespacedKey("wintervillage", "shop/unique-id");
        this.amountKey = new NamespacedKey("wintervillage", "shop/increment_decrement_amount");

        new PlayerInteractEntityListener();
        new SignChangeListener();
    }

    public Optional<Shop> raytrace(Player player) {
        Location startLocation = player.getEyeLocation().clone();
        Vector direction = startLocation.getDirection().normalize();

        List<Entity> nearby = player.getNearbyEntities(10.0d, 10.0d, 10.0d);

        for (double i = 0; i <= 10.0d; i += .1) {
            Vector point = startLocation.toVector().add(direction.clone().multiply(i));

            for (Entity entity : nearby) {
                if (!(entity instanceof Interaction)) continue;
                BoundingBox boundingBox = entity.getBoundingBox();
                if (boundingBox.contains(point)) return this.byLocation(point.toLocation(entity.getWorld()));
            }
        }

        return Optional.empty();
    }

    public Optional<Shop> byUniqueId(UUID uniqueId) {
        return this.shops.stream()
                .filter(shop -> shop.uniqueId().equals(uniqueId))
                .findFirst();
    }

    public Optional<Shop> byLocation(Location location) {
        return this.shops.stream()
                .filter(shop -> shop.location().distance(location) < .5
                        || location.getNearbyEntities(.5, .5, .5).stream()
                        .anyMatch(entity -> entity.getPersistentDataContainer().has(this.shopKey, PersistentDataType.STRING)
                                && entity.getPersistentDataContainer().get(this.shopKey, PersistentDataType.STRING).equals(shop.uniqueId().toString())))
                .findFirst();
    }

    public void forceUpdate(BiConsumer<Boolean, String> feedback) {
        this.winterVillage.shopDatabase.find()
                .thenAccept(shops -> {
                    Bukkit.getScheduler().runTask(this.winterVillage, () -> {
                        if (!this.shops.isEmpty()) this.clearShops();
                        this.loadShops(shops);
                    });

                    feedback.accept(true, "Shops loaded successfully.");
                })
                .exceptionally(throwable -> {
                    feedback.accept(false, throwable.getMessage());
                    return null;
                });
    }

    public synchronized void clearShops() {
        this.shops.forEach(Shop::removeInformation);
        this.shops.clear();
    }

    public synchronized void loadShops(Collection<Shop> shops) {
        this.shops.addAll(shops);
        shops.forEach(Shop::setupInformation);
    }

    public void addShop(Shop shop) {
        this.shops.add(shop);
    }

    public void removeShop(Shop shop) {
        this.shops.remove(shop);
    }

    public List<Shop> shops() {
        return new ArrayList<>(this.shops);
    }
}
