package de.wintervillage.proxy.player;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import de.wintervillage.common.core.database.exception.EntryNotFoundException;
import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.core.player.combined.CombinedPlayer;
import de.wintervillage.common.core.player.impl.WinterVillagePlayerImpl;
import de.wintervillage.proxy.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.model.user.User;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerChatListener {

    private final WinterVillage plugin;

    public PlayerChatListener(WinterVillage plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LAST)
    public EventTask execute(PlayerChatEvent event) {
        return EventTask.withContinuation(continuation -> {
            final UUID uniqueId = event.getPlayer().getUniqueId();

            // load User (LuckPerms) and WinterVillagePlayer and combine them
            CompletableFuture<User> userFuture = this.plugin.luckPerms.getUserManager().loadUser(uniqueId);
            CompletableFuture<WinterVillagePlayer> playerFuture = this.plugin.playerDatabase.player(uniqueId)
                    .exceptionally(throwable -> {
                        if (throwable instanceof EntryNotFoundException) {
                            WinterVillagePlayer player = new WinterVillagePlayerImpl(uniqueId);
                            this.plugin.playerDatabase.insert(player);
                            return player;
                        }
                        throw new RuntimeException(throwable);
                    });

            userFuture.thenCombine(playerFuture, CombinedPlayer::new)
                    .thenAccept(combinedResult -> {
                        final User user = combinedResult.user();
                        final WinterVillagePlayer winterVillagePlayer = combinedResult.winterVillagePlayer();

                        // Cancelled
                        // | If the player has an active muteInformation and is not able to bypass it
                        if (winterVillagePlayer.muteInformation() != null && !user.getCachedData().getPermissionData().checkPermission("wintervillage.mute-bypass").asBoolean()) {
                            event.getPlayer().sendMessage(Component.text("Du wurdest stummgeschalten", NamedTextColor.RED)
                                    .append(Component.newline())
                                    .append(Component.text("Grund: " + winterVillagePlayer.muteInformation().reason(), NamedTextColor.RED)));
                            event.setResult(PlayerChatEvent.ChatResult.denied());
                            continuation.resume();
                            return;
                        }

                        // TODO: pass through messages in "event" server, sync messages
                        event.setResult(PlayerChatEvent.ChatResult.allowed());
                        continuation.resume();
                    });
        });
    }
}
