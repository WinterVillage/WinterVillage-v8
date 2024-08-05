package de.wintervillage.main.player;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.player.listener.PlayerJoinListener;
import de.wintervillage.main.player.listener.PlayerQuitListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerHandler {

    private final WinterVillage winterVillage;

    private final ScheduledExecutorService executorService;

    @Inject
    public PlayerHandler() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::save, 5, 5, TimeUnit.MINUTES);

        new PlayerJoinListener();
        new PlayerQuitListener();
    }

    public void clear(Player player) {
        player.getInventory().setHeldItemSlot(0);

        player.getInventory().clear();
        player.getEnderChest().clear();
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(5);

        // TODO: clear player data
    }

    public void save() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            this.winterVillage.playerDatabase.modify(player.getUniqueId(), winterVillagePlayer -> {
                        winterVillagePlayer.playerInformation().save(player);
                    })
                    .exceptionally(throwable -> {
                        player.sendMessage(
                                Component.text("There was an error while saving your player data. Please contact an administrator!", NamedTextColor.DARK_RED)
                                        .append(Component.newline())
                                        .append(Component.text(throwable.getMessage(), NamedTextColor.DARK_RED))
                        );
                        return null;
                    });
        });
    }

    public void terminate() {
        this.executorService.shutdown();
    }
}
