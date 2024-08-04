package de.wintervillage.common.core.player;

import de.wintervillage.common.core.player.data.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

public interface WinterVillagePlayer {

    // TODO: Homes, Wildcard, Whitelist, Transactions, PlayerData (PotionEffects, Advancements, Statistics, Health, Attributes, Hunger, Level & XP)

    @NotNull
    UUID uniqueId();

    @NotNull
    BigDecimal money();

    void money(@NotNull BigDecimal money);

    @Nullable
    BanInformation banInformation();

    void banInformation(@Nullable BanInformation banInformation);

    @Nullable
    MuteInformation muteInformation();

    void muteInformation(@Nullable MuteInformation muteInformation);

    @NotNull
    PlayerInformation playerInformation();

    void playerInformation(@NotNull PlayerInformation playerInformation);

    @Nullable
    WildcardInformation wildcardInformation();

    void wildcardInformation(@Nullable WildcardInformation wildcardInformation);

    @Nullable
    WhitelistInformation whitelistInformation();

    void whitelistInformation(@Nullable WhitelistInformation whitelistInformation);
}
