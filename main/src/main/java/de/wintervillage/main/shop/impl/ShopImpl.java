package de.wintervillage.main.shop.impl;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import de.wintervillage.main.shop.data.ShopStatistics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.Binary;
import org.bson.types.Decimal128;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.math.BigDecimal;
import java.util.UUID;

import static de.wintervillage.common.core.database.UUIDConverter.fromBytes;
import static de.wintervillage.common.core.database.UUIDConverter.toBinary;

public class ShopImpl implements Shop {

    @BsonId
    private final @NotNull UUID _id;

    @BsonProperty("owner")
    private @NotNull UUID owner;

    @BsonProperty("name")
    private @NotNull String name;

    @BsonProperty("location")
    private final @NotNull Location location;

    @BsonProperty("blockFace")
    private final @NotNull BlockFace blockFace;

    @BsonProperty("item")
    private @Nullable ItemStack item;

    @BsonProperty("price")
    private @NotNull BigDecimal price;

    @BsonProperty("amount")
    private @NotNull BigDecimal amount;

    @BsonProperty("statistics")
    private @NotNull ShopStatistics statistics;

    public ShopImpl(
            UUID _id,
            UUID owner,
            String name,
            Location location,
            BlockFace blockFace,
            ItemStack item,
            BigDecimal price,
            BigDecimal amount,
            ShopStatistics statistics
    ) {
        this._id = _id;
        this.owner = owner;
        this.name = name;
        this.location = location;
        this.blockFace = blockFace;
        this.item = item;
        this.price = price;
        this.amount = amount;
        this.statistics = statistics;
    }

    @Override
    public @NotNull UUID uniqueId() {
        return this._id;
    }

    @Override
    public @NotNull UUID owner() {
        return this.owner;
    }

    @Override
    public void owner(@NotNull UUID uuid) {
        this.owner = uuid;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public void name(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull Location location() {
        return this.location;
    }

    @Override
    public @Nullable ItemStack item() {
        return this.item;
    }

    @Override
    public void item(@NotNull ItemStack item) {
        this.item = item;
    }

    @Override
    public @NotNull BigDecimal price() {
        return this.price;
    }

    @Override
    public void price(@NotNull BigDecimal price) {
        this.price = price;
    }

    @Override
    public @NotNull BigDecimal amount() {
        return this.amount;
    }

    @Override
    public void amount(@NotNull BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public @NotNull ShopStatistics statistics() {
        return this.statistics;
    }

    @Override
    public void statistics(@NotNull ShopStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public void setupInformation() {
        WinterVillage winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        Location spawnLocation = this.location.clone().add(0.5, 0.5, 0.5); // block center
        Vector direction = this.blockFace.getDirection().multiply(0.5f); // direction to block edge
        spawnLocation.add(direction).add(direction.normalize().multiply(0.025f)); // adjust to block edge and then slightly outward

        ItemDisplay itemDisplay = this.location.getWorld().spawn(spawnLocation, ItemDisplay.class, display -> {
            display.setInvulnerable(true);
            display.setNoPhysics(true);
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);

            Matrix4f itemMatrix = new Matrix4f();

            if (this.item == null) display.setItemStack(new ItemStack(Material.BARRIER));
            else {
                if (this.item.getType().isItem() && !this.item.getType().isBlock()) itemMatrix.scale(0.75f);
                display.setItemStack(this.item);
            }

            switch (this.blockFace) {
                case NORTH -> itemMatrix.rotateY((float) Math.toRadians(180));
                case SOUTH -> itemMatrix.rotateY((float) Math.toRadians(0));
                case EAST -> itemMatrix.rotateY((float) Math.toRadians(-90));
                case WEST -> itemMatrix.rotateY((float) Math.toRadians(90));
            }

            display.setTransformationMatrix(itemMatrix);

            PersistentDataContainer container = display.getPersistentDataContainer();
            container.set(winterVillage.shopHandler.shopKey, PersistentDataType.STRING, this._id.toString());
        });

        this.location.getWorld().spawn(spawnLocation.clone().add(0, .5, 0), TextDisplay.class, display -> {
            display.setInvulnerable(true);
            display.setNoPhysics(true);
            display.setDisplayWidth(100f);
            display.setShadowed(true);
            display.setBrightness(new Display.Brightness(0, 0));
            display.setBillboard(Display.Billboard.VERTICAL);

            display.text(Component.empty()
                    .append(Component.text("Verfügbar: ", NamedTextColor.WHITE)
                            .append(Component.text(winterVillage.formatBD(this.amount(), false), NamedTextColor.YELLOW)))
                    .append(Component.newline())
                    .append(Component.text("Preis: ", NamedTextColor.WHITE)
                            .append(Component.text(winterVillage.formatBD(this.price(), true) + " $", NamedTextColor.YELLOW)))
            );

            PersistentDataContainer container = display.getPersistentDataContainer();
            container.set(winterVillage.shopHandler.shopKey, PersistentDataType.STRING, this._id.toString());
        });

        Interaction hitboxItem = this.location.getWorld().spawn(spawnLocation, Interaction.class, interaction -> {
            interaction.setInvulnerable(true);
            interaction.setInteractionWidth(1f);
            interaction.setInteractionHeight(1.5f);
            interaction.setResponsive(true);

            PersistentDataContainer container = interaction.getPersistentDataContainer();
            container.set(winterVillage.shopHandler.shopKey, PersistentDataType.STRING, this._id.toString());
        });
        itemDisplay.addPassenger(hitboxItem);
    }

    @Override
    public void removeInformation() {
        WinterVillage winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.location.getWorld().getNearbyEntities(this.location, 1.5, 1.5, 1.5).stream()
                .filter(entity -> {
                    PersistentDataContainer container = entity.getPersistentDataContainer();
                    return container.has(winterVillage.shopHandler.shopKey, PersistentDataType.STRING)
                            && container.get(winterVillage.shopHandler.shopKey, PersistentDataType.STRING).equals(this._id.toString());
                })
                .forEach(Entity::remove);
    }

    @Override
    public void updateInformation() {
        WinterVillage winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.location.getWorld().getNearbyEntities(this.location, 1.5, 1.5, 1.5).stream()
                .filter(entity -> entity instanceof Display)
                .map(entity -> (Display) entity)
                .filter(display -> {
                    PersistentDataContainer container = display.getPersistentDataContainer();
                    return container.has(winterVillage.shopHandler.shopKey, PersistentDataType.STRING)
                            && container.get(winterVillage.shopHandler.shopKey, PersistentDataType.STRING).equals(this._id.toString());
                })
                .forEach(display -> {
                    if (display instanceof ItemDisplay itemDisplay) {
                        Matrix4f matrix = new Matrix4f();

                        if (this.item == null) itemDisplay.setItemStack(new ItemStack(Material.BARRIER));
                        else {
                            if (this.item.getType().isItem() && !this.item.getType().isBlock()) matrix.scale(0.75f);
                            itemDisplay.setItemStack(this.item);
                        }

                        switch (this.blockFace) {
                            case NORTH -> matrix.rotateY((float) Math.toRadians(180));
                            case SOUTH -> matrix.rotateY((float) Math.toRadians(0));
                            case EAST -> matrix.rotateY((float) Math.toRadians(-90));
                            case WEST -> matrix.rotateY((float) Math.toRadians(90));
                        }
                        display.setTransformationMatrix(matrix);
                    }

                    if (display instanceof TextDisplay textDisplay) {
                        textDisplay.text(Component.empty()
                                .append(Component.text("Verfügbar: ", NamedTextColor.WHITE)
                                        .append(Component.text(winterVillage.formatBD(this.amount(), false), NamedTextColor.YELLOW)))
                                .append(Component.newline())
                                .append(Component.text("Preis: ", NamedTextColor.WHITE)
                                        .append(Component.text(winterVillage.formatBD(this.price(), true) + " $", NamedTextColor.YELLOW)))
                        );
                    }
                });
    }

    public Document toDocument() {
        Document document = new Document("_id", toBinary(this._id))
                .append("owner", toBinary(this.owner))
                .append("name", this.name)
                .append("location", new Document("world", this.location.getWorld().getName())
                        .append("blockX", this.location.getBlockX())
                        .append("blockY", this.location.getBlockY())
                        .append("blockZ", this.location.getBlockZ()))
                .append("blockFace", this.blockFace.name())
                .append("price", this.price)
                .append("amount", this.amount)
                .append("statistics", this.statistics.toDocument());

        if (this.item != null) document.append("item", new Binary(this.item.serializeAsBytes()));
        return document;
    }

    public static ShopImpl fromDocument(Document document) {
        UUID _id = fromBytes(document.get("_id", Binary.class).getData());
        UUID owner = fromBytes(document.get("owner", Binary.class).getData());
        String name = document.getString("name");

        Document locationDocument = document.get("location", Document.class);
        Location location = new Location(
                Bukkit.getWorld(locationDocument.getString("world")),
                locationDocument.getInteger("blockX"),
                locationDocument.getInteger("blockY"),
                locationDocument.getInteger("blockZ")
        );

        BlockFace blockFace = BlockFace.valueOf(document.getString("blockFace"));

        ItemStack item = null;
        if (document.containsKey("item"))
            item = ItemStack.deserializeBytes(document.get("item", Binary.class).getData());

        BigDecimal price = document.get("price", Decimal128.class).bigDecimalValue();
        BigDecimal amount = document.get("amount", Decimal128.class).bigDecimalValue();

        ShopStatistics statistics = ShopStatistics.fromDocument(document.get("statistics", Document.class));

        return new ShopImpl(_id, owner, name, location, blockFace, item, price, amount, statistics);
    }

    @Override
    public String toString() {
        return "ShopImpl{" +
                "_id=" + this._id +
                ", owner=" + this.owner +
                ", name='" + this.name + '\'' +
                ", location=" + this.location +
                ", blockFace=" + this.blockFace.name() +
                ", item=" + this.item +
                ", price=" + this.price +
                ", amount=" + this.amount +
                ", statistics=" + this.statistics +
                '}';
    }
}
