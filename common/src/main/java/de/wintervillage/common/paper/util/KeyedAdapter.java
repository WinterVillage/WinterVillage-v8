package de.wintervillage.common.paper.util;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * See <a href="https://github.com/WiIIiam278/HuskSync/blob/master/bukkit/src/main/java/net/william278/husksync/util/BukkitKeyedAdapter.java">HuskSync</a>
 */
public class KeyedAdapter {

    @Nullable
    public static Statistic matchStatistic(@NotNull String key) {
        return getRegistryValue(Registry.STATISTIC, key);
    }

    @Nullable
    public static Material matchMaterial(@NotNull String key) {
        return getRegistryValue(Registry.MATERIAL, key);
    }

    @Nullable
    public static EntityType matchEntityType(@NotNull String key) {
        return getRegistryValue(Registry.ENTITY_TYPE, key);
    }

    private static <T extends Keyed> T getRegistryValue(@NotNull Registry<T> registry, @NotNull String keyString) {
        final NamespacedKey key = NamespacedKey.fromString(keyString);
        return key != null ? registry.get(key) : null;
    }
}
