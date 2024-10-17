package de.wintervillage.main.player.listener.cloudnet;

import de.wintervillage.common.core.player.data.HomeInformation;
import de.wintervillage.common.core.type.Pair;
import de.wintervillage.main.WinterVillage;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.EventManager;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CloudNetChannelMessageListener {

    private final WinterVillage winterVillage;

    public static final ConcurrentHashMap<UUID, Pair<Component, Location>> PENDING_REQUESTS = new ConcurrentHashMap<>();

    public CloudNetChannelMessageListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        EventManager eventManager = InjectionLayer.ext().instance(EventManager.class);
        eventManager.registerListener(this);
    }

    @EventListener
    private void handle(ChannelMessageReceiveEvent event) {
        if (event.channel().equals("teleport_player")) {
            var uniqueId = event.content().readUniqueId();

            if ("wintervillage:home".equals(event.message())) {
                HomeInformation homeInformation = event.content().readObject(HomeInformation.class);
                if (homeInformation == null) {
                    event.content().release();
                    this.winterVillage.getLogger().warning("Received a home request with null information");
                    return;
                }

                PENDING_REQUESTS.put(uniqueId, Pair.of(
                        Component.translatable("wintervillage.command.home.teleported"),
                        new Location(
                                Bukkit.getWorld(homeInformation.world()),
                                homeInformation.x(),
                                homeInformation.y(),
                                homeInformation.z(),
                                homeInformation.yaw(),
                                homeInformation.pitch()
                        )));
            }

            if ("wintervillage:farmwelt".equals(event.message()))
                PENDING_REQUESTS.put(uniqueId, Pair.of(
                        Component.translatable("wintervillage.command.farmwelt.teleported"),
                        Bukkit.getWorld("world").getSpawnLocation()
                ));

            if ("wintervillage:bauwelt".equals(event.message()))
                PENDING_REQUESTS.put(uniqueId, Pair.of(
                        Component.translatable("wintervillage.command.bauwelt.teleported"),
                        Bukkit.getWorld("world").getSpawnLocation()
                ));

            event.content().release();
        }
    }

    public void processRequest(Player player) {
        PENDING_REQUESTS.computeIfPresent(player.getUniqueId(), (uuid, pair) -> {
            player.teleportAsync(
                    pair.second(),
                    PlayerTeleportEvent.TeleportCause.PLUGIN
            );
            player.sendMessage(Component.join(
                    this.winterVillage.prefix,
                    pair.first()
            ));
            return PENDING_REQUESTS.remove(uuid);
        });
    }
}
