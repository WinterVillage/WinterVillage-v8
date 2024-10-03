package de.wintervillage.main.player;

import com.comphenix.protocol.ProtocolManager;
import com.google.inject.Inject;
import de.wintervillage.common.core.player.database.PlayerDatabase;
import de.wintervillage.common.core.uuid.MojangFetcher;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.player.listener.HomeRequest;
import de.wintervillage.main.player.listener.PlayerJoinListener;
import de.wintervillage.main.player.listener.PlayerQuitListener;
import de.wintervillage.main.player.listener.packet.AdvancementPacketListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerHandler {

    private final WinterVillage winterVillage;
    private final PlayerDatabase playerDatabase;
    private final LuckPerms luckPerms;

    private final ScheduledExecutorService executorService;

    public final NamespacedKey applyingKey;

    @Inject
    public PlayerHandler(
            ProtocolManager protocolManager,
            PlayerDatabase playerDatabase,
            LuckPerms luckPerms
    ) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.playerDatabase = playerDatabase;
        this.luckPerms = luckPerms;

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::save, 5, 5, TimeUnit.MINUTES);

        this.applyingKey = new NamespacedKey("wintervillage", "playerhandler/applying_player_information");

        protocolManager.addPacketListener(new AdvancementPacketListener(this.winterVillage, this));

        // cloudnet
        new HomeRequest();

        // bukkit
        new PlayerJoinListener(this);
        new PlayerQuitListener(this);
    }

    /**
     * Clears the player data
     *
     * @param player {@link Player} to clear
     */
    public void clear(Player player) {
        player.getInventory().setHeldItemSlot(0);

        Registry.ADVANCEMENT.forEach(advancement -> {
            final AdvancementProgress progress = player.getAdvancementProgress(advancement);
            progress.getAwardedCriteria().forEach(progress::revokeCriteria);
        });

        player.getInventory().clear();
        player.getEnderChest().clear();
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setHealthScale(20.0d);

        player.setFoodLevel(20);
        player.setExhaustion(0.0f);
        player.setSaturation(5.0f);

        player.setAllowFlight(false);
        player.setFlying(false);

        player.setGameMode(GameMode.SURVIVAL);

        player.setFireTicks(0);
        player.setFallDistance(0);

        player.setExp(0.0f);
        player.setLevel(0);
    }

    /**
     * Applies the saved data to the player
     *
     * @param player    {@link Player} to apply the data to
     * @param applyFrom {@link UUID} to apply the data from
     */
    public void apply(Player player, UUID applyFrom) {
        this.clear(player);

        player.addPotionEffects(List.of(
                new PotionEffect(PotionEffectType.BLINDNESS, 80, 255, true, false),
                new PotionEffect(PotionEffectType.SLOWNESS, 80, 255, true, false)
        ));
        player.getPersistentDataContainer().set(this.applyingKey, PersistentDataType.BOOLEAN, true);
        player.showTitle(Title.title(
                Component.translatable("wintervillage.playerhandler.loading.title"),
                Component.translatable("wintervillage.playerhandler.loading.subtitle"),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(4), Duration.ofMillis(500))
        ));

        // try to load data after 3 seconds to get the newest shit
        new BukkitRunnable() {
            @Override
            public void run() {
                playerDatabase.player(applyFrom)
                        .thenAccept(winterVillagePlayer -> {
                            Bukkit.getScheduler().runTask(winterVillage, () -> winterVillagePlayer.playerInformation().apply(player)); // run on next tick to avoid applying data to the player asynchronously
                            Bukkit.getScheduler().runTaskLater(winterVillage, () -> player.getPersistentDataContainer().remove(applyingKey), 10 * 20L);

                            player.sendMessage(Component.join(
                                    winterVillage.prefix,
                                    Component.translatable("wintervillage.playerhandler.loading-successfull")
                            ));
                        })
                        .exceptionally(throwable -> {
                            player.sendMessage(Component.join(
                                    winterVillage.prefix,
                                    Component.translatable("wintervillage.playerhandler.loading-failed", Component.text(throwable.getMessage()))
                            ));
                            return null;
                        });
            }
        }.runTaskLater(this.winterVillage, 60L);
    }

    /**
     * Saves the player data
     *
     * @param player  {@link Player} to save from
     * @param applyTo {@link UUID} to save to
     */
    public void save(Player player, UUID applyTo) {
        this.playerDatabase.modify(applyTo, winterVillagePlayer -> winterVillagePlayer.playerInformation().save(player))
                .exceptionally(throwable -> {
                    player.sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.playerhandler.saving-failed", Component.text(throwable.getMessage()))
                    ));
                    return null;
                });
    }

    /**
     * Gets the highest group of the given player
     *
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
        return this.winterVillage.luckPerms.getUserManager().loadUser(uniqueId).thenApply(this::highestGroup);
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
    public CompletableFuture<Optional<UUID>> lookupUniqueId(String username) {
        return this.winterVillage.luckPerms.getUserManager().lookupUniqueId(username)
                .thenApply(Optional::ofNullable)
                .exceptionally(throwable -> Optional.empty())
                .thenCompose(uuidOptional -> {
                    if (uuidOptional.isPresent()) {
                        // found uuid
                        return CompletableFuture.completedFuture(uuidOptional);
                    } else {
                        // did not found uuid -> try to fetch from mojang & save to luckperms
                        return new MojangFetcher().lookupUniqueId(username)
                                .thenCompose(mojangUuidOptional -> {
                                    if (mojangUuidOptional.isPresent()) {
                                        UUID uuid = mojangUuidOptional.get();
                                        return this.winterVillage.luckPerms.getUserManager().savePlayerData(uuid, username)
                                                .thenApplyAsync(playerSaveResult -> Optional.of(uuid));
                                    } else return CompletableFuture.completedFuture(Optional.empty());
                                });
                    }
                });
    }

    /**
     * Saves the player data
     *
     * @param player {@link Player} to save from
     */
    public void save(Player player) {
        this.save(player, player.getUniqueId());
    }

    /**
     * Saves all online players
     */
    public void save() {
        Bukkit.getOnlinePlayers().forEach(this::save);
    }

    /**
     * Terminates the {@link ScheduledExecutorService}
     */
    public void terminate() {
        this.executorService.shutdown();
    }
}
