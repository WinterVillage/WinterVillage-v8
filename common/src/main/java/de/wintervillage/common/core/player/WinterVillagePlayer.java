package de.wintervillage.common.core.player;

import de.wintervillage.common.core.player.data.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.math.BigDecimal;
import java.util.UUID;

public interface WinterVillagePlayer {

    // TODO: TransactionInformation

    @NotNull
    UUID uniqueId();

    @NotNull
    BigDecimal money();

    void money(@NotNull BigDecimal money);

    int deaths();

    void deaths(@Range(from = 0, to = Integer.MAX_VALUE) int deaths);

    @Nullable
    BanInformation banInformation();

    void banInformation(@Nullable BanInformation banInformation);

    @Nullable
    MuteInformation muteInformation();

    void muteInformation(@Nullable MuteInformation muteInformation);

    @NotNull
    PlayerInformation playerInformation();

    void playerInformation(@NotNull PlayerInformation playerInformation);

    WildcardInformation wildcardInformation();

    void wildcardInformation(WildcardInformation wildcardInformation);

    @Nullable
    WhitelistInformation whitelistInformation();

    void whitelistInformation(@Nullable WhitelistInformation whitelistInformation);

    @Nullable
    HomeInformation homeInformation();

    void homeInformation(@Nullable HomeInformation homeInformation);
}
