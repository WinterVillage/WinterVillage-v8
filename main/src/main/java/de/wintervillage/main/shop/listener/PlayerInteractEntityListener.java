package de.wintervillage.main.shop.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import de.wintervillage.main.shop.inventory.BuyingInventory;
import de.wintervillage.main.shop.inventory.EditingInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class PlayerInteractEntityListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerInteractEntityListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void execute(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (!(event.getRightClicked() instanceof Interaction interaction)) return;

        PersistentDataContainer container = interaction.getPersistentDataContainer();
        if (!container.has(this.winterVillage.shopHandler.shopKey, PersistentDataType.STRING)) return;
        UUID uniqueId = UUID.fromString(container.get(this.winterVillage.shopHandler.shopKey, PersistentDataType.STRING));

        Optional<Shop> optional = this.winterVillage.shopHandler.byUniqueId(uniqueId);
        if (optional.isEmpty()) return;

        Shop shop = optional.get();

        if (shop.owner().equals(player.getUniqueId())) {
            this.handleAsOwner(event, shop);
            return;
        }

        if (shop.item() == null) {
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.shop.no-item-being-selled")
            ));
            return;
        }

        if (shop.amount().compareTo(BigDecimal.ZERO) <= 0) {
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.shop.shop-is-empty")
            ));
            return;
        }

        // open inventory
        BuyingInventory inventory = new BuyingInventory(shop);
        inventory.getGui().open(player);
    }

    private void handleAsOwner(PlayerInteractEntityEvent event, Shop shop) {
        Player player = event.getPlayer();
        if (shop.item() == null) {
            // set item - sneaking is required
            if (!player.isSneaking()) {
                player.sendMessage(Component.join(
                        this.winterVillage.prefix,
                        Component.translatable("wintervillage.shop.sneak-to-set-item")
                ));
                return;
            }

            // AIR is not allowed
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                player.sendMessage(Component.join(
                        this.winterVillage.prefix,
                        Component.translatable("wintervillage.shop.no-item-in-hand")
                ));
                return;
            }

            // set item
            this.winterVillage.shopDatabase.modify(shop.uniqueId(), builder -> builder.item(player.getInventory().getItemInMainHand()))
                    .thenAccept(action -> {
                        Bukkit.getScheduler().runTask(this.winterVillage, () -> {
                            shop.item(player.getInventory().getItemInMainHand());
                            shop.updateInformation();
                        });

                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.shop.item-set")
                        ));
                    })
                    .exceptionally(throwable -> {
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.shop.item-set-failed",
                                        Component.text(throwable.getMessage())
                                )
                        ));
                        return null;
                    });
            return;
        }

        // open inventory
        EditingInventory inventory = new EditingInventory(shop);
        inventory.getGui().open(player);
    }
}
