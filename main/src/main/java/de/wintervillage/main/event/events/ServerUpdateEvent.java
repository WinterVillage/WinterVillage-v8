package de.wintervillage.main.event.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public ServerUpdateEvent(){}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
