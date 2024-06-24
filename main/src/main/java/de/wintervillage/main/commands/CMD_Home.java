package de.wintervillage.main.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.config.Document;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class CMD_Home {

    private final WinterVillage wintervillage;

    private final File homePath;
    private Document homeDocument;

    public CMD_Home(Commands commands){
        this.wintervillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.homePath = new File(this.wintervillage.getDataFolder(), "homes.json");

        if(this.homePath.exists()){
            this.homeDocument = Document.load(this.homePath.toPath());
        } else {
            this.homeDocument = null;
        }

        this.register(commands);
    }

    private void register(Commands commands) {
        final LiteralArgumentBuilder<CommandSourceStack> home_builder = Commands.literal("home")
                .executes((source) -> {

                    if (!(source.getSource().getExecutor() instanceof Player player)) {
                        source.getSource().getExecutor().sendMessage(this.wintervillage.PREFIX + "Dieser Command ist nur durch einen Spieler ausführbar.");
                        return 0;
                    }

                    if(this.homeDocument == null){
                        source.getSource().getExecutor().sendMessage(this.wintervillage.PREFIX + "Du hast noch kein Zuhause gesetzt.");
                        return 0;
                    }

                    String json_key = "location." + player.getUniqueId().toString().toLowerCase();
                    JsonElement element = this.homeDocument.getElement(json_key);

                    if(element == null){
                        player.sendMessage(this.wintervillage.PREFIX + "Du hast noch kein Zuhause gesetzt.");
                        return 0;
                    }

                    JsonObject json_object = element.getAsJsonObject();

                    String world = json_object.get("world").getAsString();

                    int x = json_object.get("x").getAsInt();
                    int y = json_object.get("y").getAsInt();
                    int z = json_object.get("z").getAsInt();

                    if(Bukkit.getWorld(world) == null){
                        player.sendMessage(this.wintervillage.PREFIX + "Es ist ein Fehler aufgetreten, setze dein Zuhause neu.");
                        return 0;
                    }

                    Location location = new Location(Bukkit.getWorld(world), x, y, z);

                    if(location != null) {
                        player.teleport(location);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 3.0f);
                    }

                    return 1;
                });
    }

}
