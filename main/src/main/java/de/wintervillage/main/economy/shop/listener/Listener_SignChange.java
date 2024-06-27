package de.wintervillage.main.economy.shop.listener;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Listener_SignChange implements Listener {

    private WinterVillage winterVillage;

    public Listener_SignChange(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event){
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();;

        if(event.line(0).contains(Component.text("[Shop]"))
            && !Objects.requireNonNull(event.line(1)).toString().isEmpty()
            && !Objects.requireNonNull(event.line(2)).toString().isEmpty()) {



        }
    }

}
