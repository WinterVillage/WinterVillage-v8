package de.wintervillage.main.shop.inventory;

import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.paper.item.ItemBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import de.wintervillage.main.shop.data.ShopStatistics;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BuyingInventory {

    private final WinterVillage winterVillage;
    private final @NotNull Shop shop;

    private final Gui gui;

    private final ItemStack buyItem;

    private int buyingAmount = 1;

    public BuyingInventory(@NotNull Shop shop) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.shop = shop;

        this.gui = Gui.gui()
                .rows(5)
                .disableAllInteractions()
                .title(Component.text(shop.name(), NamedTextColor.BLUE).decorate(TextDecoration.BOLD))
                .create();

        this.gui.setDefaultClickAction(event -> {
            Player player = (Player) event.getWhoClicked();

            ItemStack itemStack = event.getCurrentItem();
            if (itemStack == null) return;

            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
            if (!container.has(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER)) return;

            int amount = container.get(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER);

            // new buyingAmount can not be less than 1, or the inventory does not have enough space
            if (this.buyingAmount + amount < 1
                    || !this.isFree(player.getInventory(), this.buyingAmount + amount)) {
                player.playSound(Sound.sound(Key.key("entity.pillager.ambient"), Sound.Source.HOSTILE, 2f, 0.6f));
                return;
            }

            // check if shop has enough items
            BigDecimal requestedAmount = BigDecimal.valueOf(this.buyingAmount + amount);
            if (requestedAmount.compareTo(this.shop.amount()) > 0) {
                player.playSound(Sound.sound(Key.key("entity.pillager.ambient"), Sound.Source.HOSTILE, 2f, 0.6f));
                player.sendMessage(Component.join(
                        this.winterVillage.prefix,
                        Component.translatable("wintervillage.shop.shop-is-empty")
                ));
                return;
            }

            // adjust amount
            this.incrementOrDecrement(amount);
        });

        // -
        this.gui.setItem(2, 2, new GuiItem(ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                .name(Component.text("-10", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER, -10))
                .build()));
        this.gui.setItem(2, 3, new GuiItem(ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                .name(Component.text("-1", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER, -1))
                .build()));

        // showcase
        this.gui.setItem(2, 5, new GuiItem(ItemBuilder.from(shop.item()).build()));

        // +
        this.gui.setItem(2, 7, new GuiItem(ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                .name(Component.text("+1", NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER, +1))
                .build()));
        this.gui.setItem(2, 8, new GuiItem(ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                .name(Component.text("+10", NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.winterVillage.shopHandler.amountKey, PersistentDataType.INTEGER, +10))
                .build()));

        // buy
        this.buyItem = ItemBuilder.from(Material.KNOWLEDGE_BOOK)
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
                .build();

        this.gui.setItem(4, 5, new GuiItem(this.buyItem, event -> {
            Player player = (Player) event.getWhoClicked();
            BigDecimal finalPrice = this.shop.price().multiply(BigDecimal.valueOf(this.buyingAmount));

            ShopStatistics statistics = new ShopStatistics();
            statistics.earned(shop.statistics().earned().add(finalPrice));
            statistics.sold(shop.statistics().sold().add(BigDecimal.valueOf(this.buyingAmount)));

            CompletableFuture<Shop> shopFuture = this.winterVillage.shopDatabase.modify(
                    this.shop.uniqueId(),
                    builder -> {
                        if (builder.amount().compareTo(BigDecimal.valueOf(this.buyingAmount)) < 0)
                            throw new IllegalArgumentException("Shop has not enough items");

                        builder.amount(builder.amount().subtract(BigDecimal.valueOf(this.buyingAmount)));
                        builder.statistics(statistics);
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
                    this.shop.owner(),
                    builder -> builder.money(builder.money().add(finalPrice))
            );

            CompletableFuture<Void> combined = CompletableFuture.allOf(buyerFuture, shopFuture/**, sellerFuture*/);
            combined.thenAccept(_ -> {
                        Bukkit.getScheduler().runTask(winterVillage, () -> {
                            BigDecimal amount = this.shop.amount();
                            amount = amount.subtract(BigDecimal.valueOf(this.buyingAmount));

                            this.shop.statistics(statistics);
                            this.shop.amount(amount);
                            this.shop.updateInformation();

                            for (int i = 0; i < this.buyingAmount; i++)
                                player.getInventory().addItem(this.shop.item());

                            player.sendMessage(Component.join(
                                    this.winterVillage.prefix,
                                    Component.translatable("wintervillage.shop.item-bought",
                                            Component.text(this.buyingAmount),
                                            Component.translatable(this.shop.item().getType().getItemTranslationKey()),
                                            Component.text("" + this.shop.price().multiply(BigDecimal.valueOf(this.buyingAmount))))
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

            this.gui.close(player);
        }));
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

    public void incrementOrDecrement(int amount) {
        this.buyingAmount += amount;

        this.buyItem.editMeta(lore -> {
            lore.lore(
                    List.of(
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
                    ));
        });
        this.gui.updateItem(4, 5, this.buyItem);
        this.gui.update();
    }

    public Gui getGui() {
        return this.gui;
    }

    @NotNull
    public Shop getShop() {
        return this.shop;
    }
}