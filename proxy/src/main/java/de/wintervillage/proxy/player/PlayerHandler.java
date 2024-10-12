package de.wintervillage.proxy.player;

import com.google.inject.Inject;
import com.velocitypowered.api.proxy.Player;
import de.wintervillage.common.core.database.exception.EntryNotFoundException;
import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.core.player.impl.WinterVillagePlayerImpl;
import de.wintervillage.common.core.type.Pair;
import de.wintervillage.common.core.uuid.MojangFetcher;
import de.wintervillage.proxy.WinterVillage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerHandler {

    private final WinterVillage winterVillage;

    private final LuckPerms luckPerms;

    @Inject
    public PlayerHandler(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;
        this.luckPerms = LuckPermsProvider.get();
    }

    /**
     * Gets the highest group of the given player
     * <p>
     * Note: This method should be used only for online-players
     *
     * @param player {@link Player} to get the group from
     * @return {@link Group} of the player
     */
    public Group highestGroup(Player player) {
        User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());

        Collection<Group> groups = user.getInheritedGroups(this.luckPerms.getPlayerAdapter(Player.class).getQueryOptions(player));
        return groups.stream()
                .max(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
                .orElse(this.luckPerms.getGroupManager().getGroup("default"));
    }

    /**
     * Gets the highest group of the given user
     *
     * @param user {@link User} to get the group from
     * @return {@link Group} containing the group of the player
     */
    public Group highestGroup(User user) {
        Collection<Group> groups = user.getInheritedGroups(user.getQueryOptions());
        return groups.stream()
                .max(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
                .orElse(this.luckPerms.getGroupManager().getGroup("default"));
    }

    /**
     * Gets the highest group of the given {@link UUID} asynchronously
     *
     * @param uniqueId {@link UUID} to get the group from
     * @return {@link CompletableFuture} containing the {@link Group} of the player
     */
    public CompletableFuture<Group> highestGroup(UUID uniqueId) {
        return this.luckPerms.getUserManager().loadUser(uniqueId).thenApply(this::highestGroup);
    }

    /**
     * Performs a high-level asynchronous operation to retrieve a {@link UUID} by the given username
     * <p>
     * It first attempts to retrieve the UUID from LuckPerms's user manager. If the UUID is not found or,
     * an error occurs, it falls back to Mojang's API to obtain the UUID. If Mojang successfully retrieves
     * the UUID, the method then saves the player data to LuckPerm's user manager for future reference.
     * <p>
     * May be long-running due to network requests
     *
     * @param username {@link String} username to lookup
     * @return {@link CompletableFuture} containing an {@link Optional} {@link UUID} of the player
     */
    public CompletableFuture<UUID> lookupUniqueId(String username) {
        return this.luckPerms.getUserManager().lookupUniqueId(username)
                .thenCompose(uniqueId -> {
                    if (uniqueId != null) return CompletableFuture.completedFuture(uniqueId);
                    return new MojangFetcher().lookupUniqueId(username)
                            .thenCompose(mojangUuidOptional -> {
                                if (mojangUuidOptional.isPresent()) {
                                    UUID uuid = mojangUuidOptional.get();
                                    return this.luckPerms.getUserManager().savePlayerData(uuid, username).thenApplyAsync(_ -> uuid);
                                } else
                                    return CompletableFuture.failedFuture(new EntryNotFoundException("Player not found"));
                            });
                });
    }

    public CompletableFuture<Pair<User, WinterVillagePlayer>> combinedPlayer(@NotNull UUID uniqueId, @Nullable String username) {
        CompletableFuture<User> userFuture = this.luckPerms.getUserManager().loadUser(uniqueId, username);
        CompletableFuture<WinterVillagePlayer> playerFuture = this.winterVillage.playerDatabase.player(uniqueId)
                .exceptionally(throwable -> {
                    if (throwable instanceof EntryNotFoundException) {
                        WinterVillagePlayer player = new WinterVillagePlayerImpl(uniqueId);
                        this.winterVillage.playerDatabase.insert(player);
                        return player;
                    }
                    throw new RuntimeException(throwable);
                });
        return userFuture.thenCombine(playerFuture, Pair::of);
    }
}
