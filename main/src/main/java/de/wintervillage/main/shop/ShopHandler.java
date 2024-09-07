package de.wintervillage.main.shop;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.listener.InventoryCloseListener;
import de.wintervillage.main.shop.listener.PlayerInteractEntityListener;
import de.wintervillage.main.shop.listener.SignChangeListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ShopHandler {

    private final WinterVillage winterVillage;

    private final List<Shop> shops;

    public final NamespacedKey shopKey;

    @Inject
    public ShopHandler() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.shops = new ArrayList<>();

        this.shopKey = new NamespacedKey("wintervillage", "shop_id");

        new InventoryCloseListener();
        new PlayerInteractEntityListener();
        new SignChangeListener();

        // this.forceUpdate();
    }

    public Optional<Shop> byUniqueId(UUID uniqueId) {
        return this.shops.stream()
                .filter(shop -> shop.uniqueId().equals(uniqueId))
                .findFirst();
    }

    public Optional<Shop> byLocation(Location location) {
        return this.shops.stream()
                .filter(shop -> shop.location().equals(location) && location.getNearbyEntities(1.5, 1.5, 1.5).stream()
                            .anyMatch(entity -> entity.getPersistentDataContainer().has(this.shopKey, PersistentDataType.STRING)
                                    && entity.getPersistentDataContainer().get(this.shopKey, PersistentDataType.STRING).equals(shop.uniqueId().toString())))
                .findFirst();
    }

    public void forceUpdate() {
        this.winterVillage.shopDatabase.find()
                .thenAccept(shops -> {
                    this.shops.clear();
                    this.shops.addAll(shops);

                    Bukkit.getScheduler().runTask(this.winterVillage, () -> shops.forEach(Shop::setupInformation));
                })
                .exceptionally(t -> {
                    this.winterVillage.getLogger().warning("Could not load shops: " + t.getMessage());
                    return null;
                });
    }

    public void terminate() {
        this.shops.forEach(Shop::removeInformation);
    }

    public void addShop(Shop shop) {
        this.shops.add(shop);
    }

    public List<Shop> shops() {
        return new ArrayList<>(this.shops);
    }
}
