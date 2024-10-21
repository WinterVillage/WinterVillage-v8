package de.wintervillage.proxy.player.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import de.wintervillage.proxy.WinterVillage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class PlayerTimeCalculation {

    private final WinterVillage winterVillage;

    public PlayerTimeCalculation(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;
    }

    @Subscribe(order = PostOrder.CUSTOM, priority = Short.MAX_VALUE)
    public EventTask execute(LoginEvent event) {
        return EventTask.withContinuation(continuation -> {
            final UUID uuid = event.getPlayer().getUniqueId();

            this.winterVillage.playerDatabase.player(uuid)
                    .thenAccept(winterVillagePlayer -> {
                        if (!winterVillagePlayer.wildcardInformation().obtainable()) return;
                        this.winterVillage.playerHandler.playTime.put(uuid, LocalDateTime.now());
                    });

            continuation.resume();
        });
    }

    @Subscribe(order = PostOrder.CUSTOM, priority = Short.MAX_VALUE)
    public EventTask execute(DisconnectEvent event) {
        return EventTask.withContinuation(continuation -> {
            final UUID uniqueId = event.getPlayer().getUniqueId();

            if (!this.winterVillage.playerHandler.playTime.containsKey(uniqueId)) {
                continuation.resume();
                return;
            }
            LocalDateTime joinTime = this.winterVillage.playerHandler.playTime.remove(uniqueId);

            long duration = Duration.between(joinTime, LocalDateTime.now()).toMillis();
            this.winterVillage.playerDatabase.modify(uniqueId, builder -> builder.playTime(builder.playTime() + duration));

            continuation.resume();
        });
    }
}
