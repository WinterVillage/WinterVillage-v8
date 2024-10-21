package de.wintervillage.proxy.player.task;

import com.google.inject.Inject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.wintervillage.proxy.WinterVillage;
import net.kyori.adventure.text.Component;
import net.luckperms.api.model.group.Group;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WildcardTask {

    private final WinterVillage winterVillage;
    private final ScheduledExecutorService executorService;

    private final ProxyServer proxyServer;

    @Inject
    public WildcardTask(WinterVillage winterVillage, ProxyServer proxyServer) {
        this.winterVillage = winterVillage;
        this.executorService = Executors.newScheduledThreadPool(1);

        this.proxyServer = proxyServer;

        this.start();
    }

    private void start() {
        this.executorService.scheduleAtFixedRate(() -> this.winterVillage.playerHandler.playTime.forEach((uuid, joinTime) -> this.proxyServer.getPlayer(uuid).ifPresent(player -> this.processDistribution(joinTime, player))), 0, 30, TimeUnit.SECONDS);
    }

    public void terminate() {
        this.executorService.shutdown();
    }

    private void processDistribution(LocalDateTime joined, @NotNull Player player) {
        Group highestGroup = this.winterVillage.playerHandler.highestGroup(player);
        int groupWeight = highestGroup.getWeight().orElse(0); // 100 eq. >= ContentCreator, < 100 = Teilnehmer

        int maxObtainable = (groupWeight >= 100 ? Integer.MAX_VALUE : 2);
        long obtainableAfter = (groupWeight >= 100 ? 8 : 20) * 60 * 60 * 1000; // >= 100 every 8h, < 100 every 20h

        this.winterVillage.playerDatabase.player(player.getUniqueId()).thenAccept(winterVillagePlayer -> {
            if (!winterVillagePlayer.wildcardInformation().obtainable()) return;

            long lastWildcardTime = winterVillagePlayer.wildcardInformation().lastWildcardReceived();
            long currentMillis = System.currentTimeMillis();

            // calculate total playtime
            long totalPlayTime = winterVillagePlayer.playTime() + Duration.between(joined, LocalDateTime.now()).toMillis();

            // check if totalPlayTime is sufficient for obtaining a wildcard
            if (totalPlayTime < obtainableAfter) return;

            // ensure enough time has passed
            long elapsedSinceLastWildcard = currentMillis - lastWildcardTime;
            if (elapsedSinceLastWildcard < obtainableAfter) return;

            this.winterVillage.playerDatabase.modify(player.getUniqueId(), builder -> {
                        builder.wildcardInformation().lastWildcardReceived(currentMillis);

                        int currentAmount = builder.wildcardInformation().currentAmount() + 1;
                        builder.wildcardInformation().currentAmount(currentAmount);
                        builder.wildcardInformation().totalReceived(builder.wildcardInformation().totalReceived() + 1);

                        if (groupWeight < 100 && currentAmount >= maxObtainable) {
                            builder.wildcardInformation().obtainable(false);
                            this.winterVillage.playerHandler.playTime.remove(player.getUniqueId());
                        }
                    })
                    .thenAccept(modifiedPlayer -> {
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.wildcard-obtained",
                                        Component.text(modifiedPlayer.wildcardInformation().currentAmount())
                                )
                        ));

                        if (!modifiedPlayer.wildcardInformation().obtainable())
                            player.sendMessage(Component.join(
                                    this.winterVillage.prefix,
                                    Component.translatable("wintervillage.wildcard-limit-reached")
                            ));
                    });
        });
    }
}
