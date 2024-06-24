package de.wintervillage.main;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class WinterVillage extends JavaPlugin {

    private final MiniMessage message = MiniMessage.miniMessage();
    public Component PREFIX;

    public boolean freeze_all;
    public ArrayList<Player> frozen_players;

    @Override
    public void onLoad() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();
        this.PREFIX = this.message.deserialize("<gradient:#d48fff:#00f7ff>WinterVillage</gradient> | <reset>");

        this.freeze_all = false;
        this.frozen_players =  new ArrayList<>();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
