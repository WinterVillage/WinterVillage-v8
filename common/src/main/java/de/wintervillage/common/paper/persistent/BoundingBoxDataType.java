package de.wintervillage.common.paper.persistent;

import de.wintervillage.common.paper.util.BoundingBox2D;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BoundingBoxDataType implements PersistentDataType<PersistentDataContainer, BoundingBox2D> {

    private final NamespacedKey minXKey, minZKey, maxXKey, maxZKey;

    public BoundingBoxDataType() {
        this.minXKey = new NamespacedKey("wintervillage", "boundingbox_min_x");
        this.minZKey = new NamespacedKey("wintervillage", "boundingbox_min_z");
        this.maxXKey = new NamespacedKey("wintervillage", "boundingbox_max_x");
        this.maxZKey = new NamespacedKey("wintervillage", "boundingbox_max_z");
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
}
