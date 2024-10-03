package de.wintervillage.main.player.listener;

import de.wintervillage.common.core.player.data.HomeInformation;
import de.wintervillage.main.WinterVillage;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.EventManager;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HomeRequest {

    private final WinterVillage winterVillage;

    public static final ConcurrentHashMap<UUID, HomeInformation> PENDING_HOME_REQUESTS = new ConcurrentHashMap<>();

    public HomeRequest() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        EventManager eventManager = InjectionLayer.ext().instance(EventManager.class);
        eventManager.registerListener(this);
    }

    @EventListener
    public void handle(ChannelMessageReceiveEvent event) {
        if ("wintervillage:home".equals(event.message()) && event.channel().equals("teleport_player")) {
            var uniqueId = event.content().readUniqueId();
            HomeInformation homeInformation = event.content().readObject(HomeInformation.class);
            if (homeInformation == null) {
                this.winterVillage.getLogger().warning("Received a home request with null information");
                return;
            }

            PENDING_HOME_REQUESTS.put(uniqueId, homeInformation);
        }
    }
}
