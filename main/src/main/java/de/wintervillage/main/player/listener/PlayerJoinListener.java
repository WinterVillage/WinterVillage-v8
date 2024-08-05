package de.wintervillage.main.player.listener;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final WinterVillage winterVillage;

    public PlayerJoinListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        this.winterVillage.playerHandler.clear(player);
        player.addPotionEffects(List.of(
                new PotionEffect(PotionEffectType.BLINDNESS, 80, 255, true, false),
                new PotionEffect(PotionEffectType.SLOWNESS, 80, 255, true, false)
        ));

        // try to load data after 3 seconds to get the newest shit
        new BukkitRunnable() {
            @Override
            public void run() {
                winterVillage.playerDatabase.player(player.getUniqueId())
                        .thenAccept(winterVillagePlayer -> {
                            Bukkit.getScheduler().runTask(winterVillage, () -> winterVillagePlayer.playerInformation().apply(player)); // run on next tick to avoid applying data to the player asynchronously

                            player.sendMessage(Component.text("Your data has been loaded!", NamedTextColor.YELLOW));
                        })
                        .exceptionally(throwable -> {
                            player.sendMessage(
                                    Component.text("There was an error while loading your player data. Please contact an administrator!", NamedTextColor.DARK_RED)
                                            .append(Component.newline())
                                            .append(Component.text(throwable.getMessage(), NamedTextColor.DARK_RED))
                            );
                            return null;
                        });
            }
        }.runTaskLater(this.winterVillage, 60L);
    }
}
