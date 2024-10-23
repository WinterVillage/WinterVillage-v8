package de.wintervillage.common.core.player;

import de.wintervillage.common.core.player.data.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

public interface WinterVillagePlayer {

    @NotNull
    UUID uniqueId();

    boolean vanished();

    void vanished(boolean vanished);

    @NotNull
    BigDecimal money();

    void money(@NotNull BigDecimal money);

    int deaths();

    void deaths(@Range(from = 0, to = Integer.MAX_VALUE) int deaths);

    long playTime();

    void playTime(long playTime);

    Collection<TransactionInformation> transactions();

    void addTransaction(@NotNull TransactionInformation transactionInformation);

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
