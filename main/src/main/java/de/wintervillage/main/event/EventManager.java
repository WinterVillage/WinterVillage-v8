package de.wintervillage.main.event;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.event.events.PlayerUpdateEvent;
import de.wintervillage.main.event.events.ServerUpdateEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventManager {

    private WinterVillage winterVillage;

    private int execution_task;

    @Inject
    public EventManager(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.winterVillage.getLogger().info("EventManager started!");

        this.execution_task = this.winterVillage.getServer().getScheduler().scheduleSyncRepeatingTask(this.winterVillage, this::callUpdateEvents, 0, 5);
    }

    public void callUpdateEvents(){
        this.winterVillage.getServer().getPluginManager().callEvent(new ServerUpdateEvent());

        //this.winterVillage.getLogger().info("ServerUpdateEvent called!");

        if(!Bukkit.getOnlinePlayers().isEmpty()){
            for(Player player : Bukkit.getOnlinePlayers()){
                this.winterVillage.getServer().getPluginManager().callEvent(new PlayerUpdateEvent(player));
            }
        }
    }

    public void stop(){
        this.winterVillage.getServer().getScheduler().cancelTask(this.execution_task);
    }

}
