package de.wintervillage.main.shop;

import de.wintervillage.main.shop.data.ShopStatistics;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

public interface Shop {

    /**
     * Unique identifier of the shop
     * @return {@link UUID} uniqueId
     */
    @NotNull UUID uniqueId();

    /**
     * Owner of the shop
     *
     * @return {@link UUID} owner
     */
    @NotNull
    UUID owner();

    /**
     * Applies the owner to the shop
     *
     * @param uuid {@link UUID} owner
     */
    void owner(@NotNull UUID uuid);

    /**
     * Name of the shop
     *
     * @return {@link String} name
     */
    @NotNull
    String name();

    /**
     * Applies the name to the shop
     *
     * @param name {@link String} name
     */
    void name(@NotNull String name);

    /**
     * Location of the shop
     *
     * @return {@link Location} location
     */
    @NotNull
    Location location();

    /**
     * ItemStack which will be sold
     *
     * @return {@link ItemStack} item
     */
    @Nullable
    ItemStack item();

    void item(@NotNull ItemStack item);

    /**
     * Price of one item
     *
     * @return {@link BigDecimal} price
     */
    @NotNull
    BigDecimal price();

    /**
     * Applies the price to the shop
     *
     * @param price {@link BigDecimal} price
     */
    void price(@NotNull BigDecimal price);

    /**
     * Current amount of the item sold
     *
     * @return {@link BigDecimal} amount
     */
    @NotNull
    BigDecimal amount();

    /**
     * Applies the current amount to the shop
     *
     * @param amount {@link BigDecimal} amount
     */
    void amount(@NotNull BigDecimal amount);

    /**
     * Statistics of the shop
     * @return {@link ShopStatistics} statistics
     */
    @NotNull
    ShopStatistics statistics();

    /**
     * Applies the statistics to the shop
     * @param statistics {@link ShopStatistics} statistics
     */
    void statistics(@NotNull ShopStatistics statistics);

    void setupInformation();

    void removeInformation();

    void updateInformation();
}
