package de.wintervillage.main;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import de.wintervillage.main.antifreezle.AntiFreezle;
import de.wintervillage.common.core.config.Document;
import de.wintervillage.common.core.player.codec.PlayerCodecProvider;
import de.wintervillage.common.core.player.database.PlayerDatabase;
import de.wintervillage.main.calendar.CalendarHandler;
import de.wintervillage.main.calendar.codec.CalenderDayCodecProvider;
import de.wintervillage.main.calendar.commands.CalendarCommand;
import de.wintervillage.main.calendar.database.CalendarDatabase;
import de.wintervillage.main.commands.FreezeCommand;
import de.wintervillage.main.commands.InventoryCommand;
import de.wintervillage.main.death.DeathManager;
import de.wintervillage.main.economy.EconomyManager;
import de.wintervillage.main.economy.commands.CMD_Transfer;
import de.wintervillage.main.economy.shop.ShopManager;
import de.wintervillage.main.event.EventManager;
import de.wintervillage.main.listener.AsyncChatListener;
import de.wintervillage.main.listener.PlayerMoveListener;
import de.wintervillage.main.player.PlayerHandler;
import de.wintervillage.main.plot.PlotCommand;
import de.wintervillage.main.plot.PlotHandler;
import de.wintervillage.main.plot.database.PlotDatabase;
import de.wintervillage.main.plot.codec.PlotCodecProvider;
import de.wintervillage.main.specialitems.SpecialItems;
import de.wintervillage.main.specialitems.commands.CMD_Disenchant;
import de.wintervillage.main.specialitems.commands.CMD_SpecialItem;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class WinterVillage extends JavaPlugin {

    public @Inject PlotDatabase plotDatabase;
    public @Inject CalendarDatabase calendarDatabase;
    public @Inject PlayerDatabase playerDatabase;

    public @Inject PlotHandler plotHandler;
    public @Inject CalendarHandler calendarHandler;
    public @Inject PlayerHandler playerHandler;
    public @Inject ShopManager shopManager;
    public @Inject EconomyManager economyManager;
    public @Inject SpecialItems specialItems;
    public @Inject EventManager eventManager;
    public @Inject DeathManager deathManager;
    public @Inject AntiFreezle antiFreezle;

    public LuckPerms luckPerms;

    public final Component PREFIX = MiniMessage.miniMessage().deserialize("<gradient:#d48fff:#00f7ff>WinterVillage</gradient> | <reset>");

    // configs
    public Document databaseDocument;

    // databases
    public MongoClient mongoClient;
    public MongoDatabase mongoDatabase;

    // keys
    public NamespacedKey frozenKey = new NamespacedKey("wintervillage", "frozen");
    public NamespacedKey calendarKey = new NamespacedKey("wintervillage", "calendar");
    public boolean PLAYERS_FROZEN = false;

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
        if (!this.databaseDocument.isEmpty()) {
            MongoCredential credential = MongoCredential.createCredential(
                    this.databaseDocument.getString("user"),
                    this.databaseDocument.getString("database"),
                    this.databaseDocument.getString("password").toCharArray()
            );

            CodecRegistry registry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(new PlotCodecProvider()),
                    CodecRegistries.fromProviders(new CalenderDayCodecProvider()),
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

        RegisteredServiceProvider<LuckPerms> luckPermsProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (luckPermsProvider != null) this.luckPerms = luckPermsProvider.getProvider();

        // inject handlers
        Injector injector = Guice.createInjector(new WinterVillageModule(this.mongoDatabase));
        injector.injectMembers(this);

        // listener
        new AsyncChatListener();
        new PlayerMoveListener();

        // commands
        final LifecycleEventManager<Plugin> lifecycleEventManager = this.getLifecycleManager();
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands command = event.registrar();

            //General-System
            new FreezeCommand(command);
            new InventoryCommand(command);
            new PlotCommand(command);

            //Economy-System TODO: proxy
            new CMD_Transfer(command);

            //SpecialItems
            new CMD_Disenchant(command);
            new CMD_SpecialItem(command);

            //AdventCalendar
            new CalendarCommand(command);
        });
    }

    @Override
    public void onDisable() {
        if (this.mongoClient != null) this.mongoClient.close();
        if (this.plotHandler != null) this.plotHandler.terminate();
        if (this.playerHandler != null) this.playerHandler.terminate();

        this.eventManager.stop();
    }
}
