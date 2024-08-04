package de.wintervillage.proxy.player;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import de.wintervillage.common.core.database.exception.EntryNotFoundException;
import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.core.player.combined.CombinedPlayer;
import de.wintervillage.common.core.player.impl.WinterVillagePlayerImpl;
import de.wintervillage.proxy.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PreLoginListener {

    private final WinterVillage plugin;

    public PreLoginListener(WinterVillage plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LAST)
    public EventTask execute(PreLoginEvent event) {
        return EventTask.withContinuation(continuation -> {
            final UUID uniqueId = event.getUniqueId();

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
                        final WinterVillagePlayer player = combinedResult.winterVillagePlayer();

                        Collection<Group> groups = user.getInheritedGroups(user.getQueryOptions());
                        Group highestGroup = groups.stream()
                                .max(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
                                .orElse(this.plugin.luckPerms.getGroupManager().getGroup("default"));

                        // Cancelled
                        // | If the player has an active banInformation and is not able to bypass it
                        if (player.banInformation() != null && !user.getCachedData().getPermissionData().checkPermission("wintervillage.ban-bypass").asBoolean()) {
                            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                                    Component.text("Du wurdest von WinterVillage gebannt", NamedTextColor.RED)
                                            .append(Component.newline())
                                            .append(Component.text("Grund: " + player.banInformation().reason(), NamedTextColor.RED))
                                            .append(Component.newline())
                                            .append(Component.newline())
                                            .append(Component.text("discord.wintervillage.de", NamedTextColor.AQUA))
                            ));
                            continuation.resume();
                            return;
                        }

                        // Allowed
                        // | If the player has a weight of 100 (content-creator) or higher
                        if (highestGroup.getWeight().orElse(0) >= 100) {
                            event.setResult(PreLoginEvent.PreLoginComponentResult.allowed());
                            continuation.resume();
                            return;
                        }

                        // Cancelled
                        // | If the player has no whitelistInformation and is not able to bypass it
                        if (player.whitelistInformation() == null || !user.getCachedData().getPermissionData().checkPermission("wintervillage.whitelist-bypass").asBoolean()) {
                            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                                    Component.text("Du bist nicht auf der Whitelist", NamedTextColor.RED)
                                            .append(Component.newline())
                                            .append(Component.text("discord.wintervillage.de", NamedTextColor.AQUA))
                            ));
                            continuation.resume();
                            return;
                        }

                        // Allowed
                        event.setResult(PreLoginEvent.PreLoginComponentResult.allowed());
                        continuation.resume();
                    });
        });
    }
}
