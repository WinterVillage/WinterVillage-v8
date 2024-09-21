package de.wintervillage.proxy.combined;

import de.wintervillage.common.core.player.combined.CombinedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PlayerPair(@NotNull CombinedPlayer first, @Nullable CombinedPlayer second) {
}
