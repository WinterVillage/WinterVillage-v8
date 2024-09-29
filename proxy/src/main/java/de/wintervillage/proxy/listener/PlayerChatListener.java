package de.wintervillage.proxy.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.proxy.WinterVillage;
import net.kyori.adventure.text.Component;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public class PlayerChatListener {

    private final WinterVillage plugin;

    public PlayerChatListener(WinterVillage plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.CUSTOM, priority = Short.MAX_VALUE)
    public EventTask execute(PlayerChatEvent event) {
        return EventTask.withContinuation(continuation -> {
            final UUID uniqueId = event.getPlayer().getUniqueId();

            this.plugin.playerHandler.combinedPlayer(uniqueId, event.getPlayer().getUsername())
                    .thenAccept(combinedResult -> {
                        final User user = combinedResult.user();
                        final WinterVillagePlayer winterVillagePlayer = combinedResult.winterVillagePlayer();

                        // Cancelled
                        // | If the player has an active muteInformation and is not able to bypass it
                        if (winterVillagePlayer.muteInformation() != null && !user.getCachedData().getPermissionData().checkPermission("wintervillage.mute-bypass").asBoolean()) {
                            event.getPlayer().sendMessage(Component.translatable("wintervillage.you-are-muted",
                                    Component.text(winterVillagePlayer.muteInformation().reason())
                            ));
                            event.setResult(PlayerChatEvent.ChatResult.denied());
                            continuation.resume();
                            return;
                        }

                        // TODO: sync?
                        event.setResult(PlayerChatEvent.ChatResult.allowed());
                        continuation.resume();
                    });
        });
    }
}
