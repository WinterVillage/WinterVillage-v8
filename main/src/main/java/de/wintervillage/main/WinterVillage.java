package de.wintervillage.main;

import org.bukkit.plugin.java.JavaPlugin;

public final class WinterVillage extends JavaPlugin {

    @Override
    public void onLoad() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();
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
