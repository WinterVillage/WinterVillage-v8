package de.wintervillage.main;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public final class WinterVillage extends JavaPlugin {

    private final MiniMessage message = MiniMessage.miniMessage();
    public Component PREFIX;

    @Override
    public void onLoad() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();
        this.PREFIX = this.message.deserialize("<gradient:#d48fff:#00f7ff>WinterVillage</gradient> | <reset>");
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
