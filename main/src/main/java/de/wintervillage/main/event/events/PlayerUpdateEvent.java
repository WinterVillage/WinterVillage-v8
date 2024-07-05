package de.wintervillage.main.event.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Player player;

    public PlayerUpdateEvent(Player player) {
        this.player = player;
    }

    @Override
    public HandlerList getHandlers(){
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

}
