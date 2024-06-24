package de.wintervillage.main;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class WinterVillage extends JavaPlugin {

    private final MiniMessage message = MiniMessage.miniMessage();
    public Component PREFIX;

    public NamespacedKey key_frozen;
    public boolean freeze_all;

    @Override
    public void onLoad() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();
        this.PREFIX = this.message.deserialize("<gradient:#d48fff:#00f7ff>WinterVillage</gradient> | <reset>");
    }

    @Override
    public void onEnable() {
        key_frozen = new NamespacedKey(this, "frozen");
        this.freeze_all = false;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
