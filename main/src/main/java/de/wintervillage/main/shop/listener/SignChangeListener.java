package de.wintervillage.main.shop.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import de.wintervillage.main.shop.data.ShopStatistics;
import de.wintervillage.main.shop.exception.ShopValidationException;
import de.wintervillage.main.shop.impl.ShopImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;

public class SignChangeListener implements Listener {

    private final WinterVillage winterVillage;

    public SignChangeListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void execute(SignChangeEvent event) {
        if (event.isCancelled())
            return; // event is cancelled -> player tries to edit a sign within a plot he's not a member of

        Player player = event.getPlayer();

        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        if (!serializer.serialize(event.line(0)).equalsIgnoreCase("[shop]")) return;

        Block attachedOn = this.attachedOn(event.getBlock());
        if (attachedOn == null) {
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.shop.no-block-attached")
            ));
            event.setCancelled(true);
            return;
        }

        // shop exists already - should not happen
        if (this.winterVillage.shopHandler.byLocation(event.getBlock().getLocation()).isPresent()
                || this.winterVillage.shopHandler.byLocation(attachedOn.getLocation()).isPresent()) {
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.shop.location-already-exists")
            ));
            event.setCancelled(true);
            return;
        }

        ValidatedShop validated = this.validate(event.line(1), event.line(2), throwable -> {
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    Component.translatable("wintervillage.shop.validation-error", Component.text(throwable.getMessage()))
            ));
        });

        if (validated == null) return;

        Shop shop = new ShopImpl(
                UUID.randomUUID(),
                player.getUniqueId(),
                validated.name,
                attachedOn.getLocation(),
                this.getBlockFace(event.getBlock()),
                null,
                validated.price,
                BigDecimal.ZERO,
                new ShopStatistics(BigDecimal.ZERO, BigDecimal.ZERO)
        );

        event.setCancelled(true);
        event.getBlock().breakNaturally();

        this.winterVillage.shopDatabase.insert(shop)
                .thenAccept(_ -> {
                    Bukkit.getScheduler().runTask(this.winterVillage, () -> {
                        this.winterVillage.shopHandler.addShop(shop);
                        shop.setupInformation();
                    }); // avoid asynchronous entity add

                    player.sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.shop.created", Component.text(validated.name))
                    ));
                })
                .exceptionally(throwable -> {
                    player.sendMessage(Component.text("An error occurred while creating the shop : " + throwable.getMessage()));
                    return null;
                });
    }

    private BlockFace getBlockFace(Block sign) {
        BlockData blockData = sign.getBlockData();
        if (blockData instanceof WallSign wallSign) return wallSign.getFacing();
        return null;
    }

    private Block attachedOn(Block sign) {
        BlockData blockData = sign.getBlockData();
        if (blockData instanceof Directional directional)
            return sign.getRelative(directional.getFacing().getOppositeFace());
        return null;
    }

    private ValidatedShop validate(Component nameLine, Component priceLine, Consumer<Throwable> consumer) {
        try {
            PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

            String name = serializer.serialize(nameLine);
            String price = serializer.serialize(priceLine);

            if (name.isEmpty()) throw new ShopValidationException("Shop name cannot be empty");

            if (!price.matches("^\\d+(\\.\\d{1,2})?$"))
                throw new ShopValidationException("Price must be a number with up to two decimal places");

            BigDecimal bigDecimal = new BigDecimal(price);
            if (bigDecimal.compareTo(BigDecimal.ZERO) <= 0)
                throw new ShopValidationException("Price must be greater than zero");

            return new ValidatedShop(name, bigDecimal);
        } catch (Throwable throwable) {
            consumer.accept(throwable);
            return null;
        }
    }

    private record ValidatedShop(String name, BigDecimal price) {
    }
}
