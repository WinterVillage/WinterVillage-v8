package de.wintervillage.proxy;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.wintervillage.common.core.config.Document;
import de.wintervillage.common.core.player.codec.PlayerCodecProvider;
import de.wintervillage.common.core.player.database.PlayerDatabase;
import de.wintervillage.proxy.player.PreLoginListener;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "wintervillage-proxy",
        name = "WinterVillage-Proxy",
        version = "8-SNAPSHOT",
        authors = {"Voldechse"},
        dependencies = {
                @Dependency(id = "luckperms", optional = false)
        }
)
public final class WinterVillage {

    private @Inject final ProxyServer proxyServer;
    private @Inject final Logger logger;
    private final Path dataDirectory;

    public PlayerDatabase playerDatabase;

    public LuckPerms luckPerms;

    // configs
    public Document databaseDocument;

    // databases
    public MongoClient mongoClient;
    public MongoDatabase mongoDatabase;

    @Inject
    public WinterVillage(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        this.resolveConfig();
        this.connect();
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        this.luckpermsSupport();

        Injector injector = Guice.createInjector(new WinterVillageModule(this.mongoDatabase));
        this.playerDatabase = injector.getInstance(PlayerDatabase.class);

        this.proxyServer.getEventManager().register(this, new PreLoginListener(this));
    }

    private void resolveConfig() {
        try {
            Path configPath = this.dataDirectory.resolve("database.json");
            Files.createDirectories(configPath.getParent());

            if (!Files.exists(configPath)) {
                new Document("host", "127.0.0.1")
                        .append("port", 27017)
                        .append("database", "database")
                        .append("user", "user")
                        .append("password", "password")
                        .save(configPath);
            }

            this.databaseDocument = Document.load(configPath);
        } catch (IOException exception) {
            this.logger.error("Failed to resolve configuration", exception);
        }
    }

    private void connect() {
        if (this.databaseDocument.isEmpty()) return;
        MongoCredential credential = MongoCredential.createCredential(
                this.databaseDocument.getString("user"),
                this.databaseDocument.getString("database"),
                this.databaseDocument.getString("password").toCharArray()
        );

        CodecRegistry registry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(new PlayerCodecProvider())
        );

        this.mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(List.of(new ServerAddress(this.databaseDocument.getString("host"), this.databaseDocument.getInt("port"))))
                        )
                        .applyToConnectionPoolSettings(builder -> builder.maxConnectionIdleTime(60, TimeUnit.SECONDS))
                        .applyToSocketSettings(builder -> builder.connectTimeout(10, TimeUnit.SECONDS))
                        .applyToServerSettings(builder -> builder.heartbeatFrequency(10, TimeUnit.SECONDS))
                        .credential(credential)
                        .codecRegistry(registry)
                        .build()
        );
        this.mongoDatabase = this.mongoClient.getDatabase(this.databaseDocument.getString("database"));
    }

    private void luckpermsSupport() {
        if (!this.proxyServer.getPluginManager().getPlugin("luckperms").isPresent()) return;

        try {
            this.luckPerms = LuckPermsProvider.get();
        } catch (Exception e) {
            this.logger.error("Failed to load LuckPerms service", e);
        }
    }
}