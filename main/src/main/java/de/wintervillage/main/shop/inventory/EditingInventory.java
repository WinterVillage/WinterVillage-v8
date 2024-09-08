package de.wintervillage.main.shop.inventory;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;

public class EditingInventory implements InventoryHolder {

    private final WinterVillage winterVillage;
    private final Inventory inventory;
    private final @NotNull Shop shop;

    private static final int INVENTORY_SIZE = 54;

    public EditingInventory(@NotNull Shop shop) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.shop = shop;

        this.inventory = this.winterVillage.getServer().createInventory(this, INVENTORY_SIZE, Component.text("Verfülle deinen Shop", NamedTextColor.BLUE).decorate(TextDecoration.BOLD));

        if (shop.item() == null) return;

        int amount = shop.amount().setScale(0, RoundingMode.DOWN).intValueExact();
        for (int i = 0; i < amount; i++) {
            ItemStack itemStack = shop.item().clone();
            itemStack.setAmount(1);
            this.inventory.addItem(itemStack);
        }
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
