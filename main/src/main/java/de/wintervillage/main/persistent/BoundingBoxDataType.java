package de.wintervillage.main.persistent;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.util.BoundingBox2D;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BoundingBoxDataType implements PersistentDataType<PersistentDataContainer, BoundingBox2D> {

    private final WinterVillage winterVillage;

    private final NamespacedKey minXKey, minZKey, maxXKey, maxZKey;

    public BoundingBoxDataType() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.minXKey = new NamespacedKey(this.winterVillage, "minX");
        this.minZKey = new NamespacedKey(this.winterVillage, "minZ");
        this.maxXKey = new NamespacedKey(this.winterVillage, "maxX");
        this.maxZKey = new NamespacedKey(this.winterVillage, "maxZ");
    }

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<BoundingBox2D> getComplexType() {
        return BoundingBox2D.class;
    }

    @Override
    public @NotNull PersistentDataContainer toPrimitive(@NotNull BoundingBox2D complex, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();

        container.set(this.minXKey, PersistentDataType.DOUBLE, complex.getMinX());
        container.set(this.minZKey, PersistentDataType.DOUBLE, complex.getMinZ());
        container.set(this.maxXKey, PersistentDataType.DOUBLE, complex.getMaxX());
        container.set(this.maxZKey, PersistentDataType.DOUBLE, complex.getMaxZ());

        return container;
    }

    @Override
    public @NotNull BoundingBox2D fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
        double minX = Objects.requireNonNull(primitive.get(this.minXKey, PersistentDataType.DOUBLE));
        double minZ = Objects.requireNonNull(primitive.get(this.minZKey, PersistentDataType.DOUBLE));
        double maxX = Objects.requireNonNull(primitive.get(this.maxXKey, PersistentDataType.DOUBLE));
        double maxZ = Objects.requireNonNull(primitive.get(this.maxZKey, PersistentDataType.DOUBLE));

        return new BoundingBox2D(minX, minZ, maxX, maxZ);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundingBoxDataType that = (BoundingBoxDataType) o;
        return Objects.equals(winterVillage, that.winterVillage) && Objects.equals(minXKey, that.minXKey) && Objects.equals(minZKey, that.minZKey) && Objects.equals(maxXKey, that.maxXKey) && Objects.equals(maxZKey, that.maxZKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(winterVillage, minXKey, minZKey, maxXKey, maxZKey);
    }
}
