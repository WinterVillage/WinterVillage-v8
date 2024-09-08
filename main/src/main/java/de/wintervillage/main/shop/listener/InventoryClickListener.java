package de.wintervillage.main.shop.listener;

import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import de.wintervillage.main.shop.inventory.BuyingInventory;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static de.wintervillage.common.paper.util.InventoryModifications.*;

public class InventoryClickListener implements Listener {

    private final WinterVillage winterVillage;

    public InventoryClickListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void execute(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getHolder(false) instanceof BuyingInventory buyingInventory) {
            // block disabled actions
            if (isPlacingItem(event) || isTakingItem(event) || isSwappingItem(event) || isDroppingItem(event) || isOtherEvent(event)) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }

            ItemStack itemStack = event.getCurrentItem();
            if (itemStack == null) return;

            // buy item
            if (itemStack.getType() == Material.KNOWLEDGE_BOOK) {
                // last check if shop has enough items
                if (BigDecimal.valueOf(buyingInventory.getBuyingAmount()).compareTo(buyingInventory.getShop().amount()) > 0) {
                    player.playSound(Sound.sound(Key.key("entity.pillager.ambient"), Sound.Source.HOSTILE, 2f, 0.6f));
                    player.sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.shop.shop-is-empty")
                    ));
                    return;
                }

                CompletableFuture<Shop> shopFuture = this.winterVillage.shopDatabase.modify(
                        buyingInventory.getShop().uniqueId(),
                        builder -> {
                            if (builder.amount().compareTo(BigDecimal.valueOf(buyingInventory.getBuyingAmount())) < 0)
                                throw new IllegalArgumentException("Shop has not enough items");

                            builder.amount(builder.amount().subtract(BigDecimal.valueOf(buyingInventory.getBuyingAmount())));
                        }
                );
                CompletableFuture<WinterVillagePlayer> buyerFuture = this.winterVillage.playerDatabase.modify(
                        player.getUniqueId(),
                        builder -> {
                            BigDecimal money = builder.money();
                            if (money.compareTo(finalPrice) < 0) throw new IllegalArgumentException("Not enough money");

                            builder.money(money.subtract(finalPrice));
                        }
                );
                CompletableFuture<WinterVillagePlayer> sellerFuture = this.winterVillage.playerDatabase.modify(
                        buyingInventory.getShop().owner(),
                        builder -> builder.money(builder.money().add(finalPrice))
                );

                CompletableFuture<Void> combined = CompletableFuture.allOf(shopFuture, buyerFuture/**, sellerFuture*/);
                combined.thenAccept(_ -> {
                            Bukkit.getScheduler().runTask(winterVillage, () -> {
                                // TODO: fix TextDisplay not being updated
                                buyingInventory.getShop().amount().subtract(BigDecimal.valueOf(buyingInventory.getBuyingAmount()));
                                buyingInventory.getShop().updateInformation();

                                for (int i = 0; i < buyingInventory.getBuyingAmount(); i++) player.getInventory().addItem(buyingInventory.getShop().item());

                                // TODO: fix item translation name
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.shop.item-bought",
                                                Component.text(buyingInventory.getBuyingAmount()),
                                                Component.translatable(buyingInventory.getShop().item().getType().getKey().toString()),
                                                Component.text("" + buyingInventory.getShop().price().multiply(BigDecimal.valueOf(buyingInventory.getBuyingAmount()))))
                                ));
                            });
                        })
                        .exceptionally(throwable -> {
                            player.sendMessage(Component.join(
                                    this.winterVillage.prefix,
                                    Component.translatable("wintervillage.shop.item-buy-failed",
                                            Component.text(throwable.getMessage())
                                    )
                            ));
                            return null;
                        });
                player.closeInventory();
                return;
            }

            // adjust amount item
            if (itemStack.getPersistentDataContainer().has(this.winterVillage.shopHandler.amountKey)) {
                int amount = itemStack.getPersistentDataContainer().get(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER);

                // new buyingAmount can not be less than 1, or the inventory does not have enough space
                if (buyingInventory.getBuyingAmount() + amount < 1
                        || !this.isFree(player.getInventory(), buyingInventory.getBuyingAmount() + amount)) {
                    player.playSound(Sound.sound(Key.key("entity.pillager.ambient"), Sound.Source.HOSTILE, 2f, 0.6f));
                    return;
                }

                // check if shop has enough items
                BigDecimal requestedAmount = BigDecimal.valueOf(buyingInventory.getBuyingAmount() + amount);
                if (requestedAmount.compareTo(buyingInventory.getShop().amount()) > 0) {
                    player.playSound(Sound.sound(Key.key("entity.pillager.ambient"), Sound.Source.HOSTILE, 2f, 0.6f));
                    player.sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.shop.shop-is-empty")
                    ));
                    return;
                }

                // adjust amount
                buyingInventory.incrementOrDecrement(amount);
                buyingInventory.update();
            }
        }
    }

    private boolean isFree(Inventory inventory, int requiredSpace) {
        int free = 0;
        for (ItemStack itemStack : inventory.getStorageContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR)
                free++;

            if (free >= requiredSpace)
                return true;
        }
        return free >= requiredSpace;
    }
}
