package de.wintervillage.main.shop.inventory;

import de.wintervillage.common.paper.item.ItemBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class BuyingInventory implements InventoryHolder {

    private final WinterVillage winterVillage;
    private final Inventory inventory;
    private final @NotNull Shop shop;

    private static final int INVENTORY_SIZE = 45;

    private int buyingAmount = 1;

    public BuyingInventory(@NotNull Shop shop) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.shop = shop;

        this.inventory = this.winterVillage.getServer().createInventory(this, INVENTORY_SIZE, Component.text(shop.name(), NamedTextColor.BLUE).decorate(TextDecoration.BOLD));

        if (shop.item() == null) return;

        // -
        this.inventory.setItem(10, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                .name(Component.text("-10", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER, -10))
                .build());
        this.inventory.setItem(11, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                .name(Component.text("-1", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER, -1))
                .build());

        // showcase
        this.inventory.setItem(13, ItemBuilder.from(shop.item()).build());

        // +
        this.inventory.setItem(15, ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                .name(Component.text("+1", NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER, +1))
                .build());
        this.inventory.setItem(16, ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                .name(Component.text("+10", NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER, +10))
                .build());

        // buy
        this.update();
    }

    public void update() {
        this.inventory.setItem(31, ItemBuilder.from(Material.KNOWLEDGE_BOOK)
                .name(Component.text("Kaufen", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text("Du kaufst gerade ", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
                                .append(Component.text(this.buyingAmount, NamedTextColor.GREEN))
                                .append(Component.text("x ", NamedTextColor.WHITE)),
                        Component.text("Preis pro Item: ", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
                                .append(Component.text("" + shop.price(), NamedTextColor.YELLOW)),
                        Component.empty(),
                        Component.text("-----------------", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.text("Du bezahlst: ", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
                                .append(Component.text("" + shop.price().multiply(BigDecimal.valueOf(this.buyingAmount)), NamedTextColor.YELLOW)),
                        Component.empty(),
                        Component.text("Klicke, um zu kaufen", NamedTextColor.GRAY)
                )
                .build());
    }

    public void incrementOrDecrement(int amount) {
        this.buyingAmount += amount;
        this.update();
    }

    public int getBuyingAmount() {
        return this.buyingAmount;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @NotNull
    public Shop getShop() {
        return this.shop;
    }
}
