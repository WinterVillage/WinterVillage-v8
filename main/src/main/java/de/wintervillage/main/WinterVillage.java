package de.wintervillage.main;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import de.wintervillage.main.commands.FreezeCommand;
import de.wintervillage.main.config.Document;
import de.wintervillage.main.listener.PlayerMoveListener;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class WinterVillage extends JavaPlugin {

    public final MiniMessage message = MiniMessage.miniMessage();
    public final Component PREFIX = this.message.deserialize("<gradient:#d48fff:#00f7ff>WinterVillage</gradient> | <reset>");

    public Document databaseDocument;

    public MongoClient mongoClient;
    public MongoDatabase mongoDatabase;

    public NamespacedKey frozenKey;
    public boolean PLAYERS_FROZEN;

    @Override
    public void onLoad() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();
        if (!Files.exists(Paths.get(this.getDataFolder().getAbsolutePath(), "database.json")))
            new Document("host", "127.0.0.1")
                    .append("port", 27017)
                    .append("database", "database")
                    .append("user", "user")
                    .append("password", "password")
                    .save(Paths.get(this.getDataFolder().getAbsolutePath(), "database.json"));
        this.databaseDocument = Document.load(Paths.get(this.getDataFolder().getAbsolutePath(), "database.json"));
    }

    @Override
    public void onEnable() {
        frozenKey = new NamespacedKey(this, "frozen");
        this.PLAYERS_FROZEN = false;

        if (!this.databaseDocument.isEmpty()) {
            MongoCredential credential = MongoCredential.createCredential(
                    this.databaseDocument.getString("user"),
                    this.databaseDocument.getString("database"),
                    this.databaseDocument.getString("password").toCharArray()
            );

            this.mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyToClusterSettings(builder ->
                                    builder.hosts(List.of(new ServerAddress(this.databaseDocument.getString("host"), this.databaseDocument.getInt("port"))))
                            )
                            .credential(credential)
                            .build()
            );
            this.mongoDatabase = this.mongoClient.getDatabase(this.databaseDocument.getString("database"));
        }

        new PlayerMoveListener(this);

        final LifecycleEventManager<Plugin> lifecycleEventManager = this.getLifecycleManager();
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands command = event.registrar();

            new FreezeCommand(command);
        });
    }

    @Override
    public void onDisable() {
        if (this.mongoClient != null) this.mongoClient.close();
    }
}
