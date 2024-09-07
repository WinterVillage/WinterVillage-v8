package de.wintervillage.main.shop.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.inventory.EditingInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Arrays;

public class InventoryCloseListener implements Listener {

    private final WinterVillage winterVillage;

    public InventoryCloseListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void execute(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!(event.getInventory().getHolder(false) instanceof EditingInventory editingInventory)) return;

        int newAmount = Arrays.stream(editingInventory.getInventory().getContents())
                .filter(itemStack -> editingInventory.getShop().item().isSimilar(itemStack))
                .mapToInt(itemStack -> itemStack.getAmount())
                .sum();

        this.winterVillage.shopDatabase.modify(editingInventory.getShop().uniqueId(), builder -> builder.amount(BigDecimal.valueOf(newAmount)))
                .thenAccept(action -> {
                    editingInventory.getShop().amount(BigDecimal.valueOf(newAmount));
                    Bukkit.getScheduler().runTask(this.winterVillage, editingInventory.getShop()::updateInformation);

                    player.sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.shop.updated-shop")
                    ));
                })
                .exceptionally(throwable -> {
                    player.sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.shop.updating-failed", throwable.getMessage())
                    ));
                    return null;
                });
    }
}
