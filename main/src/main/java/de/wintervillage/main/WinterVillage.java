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
import de.wintervillage.main.adventcalendar.AdventCalendar;
import de.wintervillage.main.adventcalendar.commands.CMD_AdventsKalender;
import de.wintervillage.main.commands.CMD_Home;
import de.wintervillage.main.commands.FreezeCommand;
import de.wintervillage.main.commands.InvseeCommand;
import de.wintervillage.main.config.Document;
import de.wintervillage.main.economy.EconomyManager;
import de.wintervillage.main.economy.commands.CMD_Transfer;
import de.wintervillage.main.economy.shop.ShopManager;
import de.wintervillage.main.event.EventManager;
import de.wintervillage.main.listener.PlayerMoveListener;
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
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class WinterVillage extends JavaPlugin {

    @Inject public PlotDatabase plotDatabase;
    @Inject public PlotHandler plotHandler;
    @Inject public ShopManager shopManager;
    @Inject public EconomyManager economyManager;
    @Inject public SpecialItems specialItems;
    @Inject public EventManager eventManager;
    @Inject public AdventCalendar adventCalendar;

    public final Component PREFIX = MiniMessage.miniMessage().deserialize("<gradient:#d48fff:#00f7ff>WinterVillage</gradient> | <reset>");

    public Document databaseDocument;

    public MongoClient mongoClient;
    public MongoDatabase mongoDatabase;

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

            CodecProvider provider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry registry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(new PlotCodecProvider(), provider)
            );

            this.mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyToClusterSettings(builder ->
                                    builder.hosts(List.of(new ServerAddress(this.databaseDocument.getString("host"), this.databaseDocument.getInt("port"))))
                            )
                            .credential(credential)
                            .build()
            );
            this.mongoDatabase = this.mongoClient.getDatabase(this.databaseDocument.getString("database"))
                    .withCodecRegistry(registry);
        }

        Injector injector = Guice.createInjector(new WinterVillageModule());
        injector.injectMembers(this);

        // TODO: Inject into WinterVillageModule

        //General-System
        new PlayerMoveListener(this);

        final LifecycleEventManager<Plugin> lifecycleEventManager = this.getLifecycleManager();
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands command = event.registrar();

            //General-System
            new FreezeCommand(command);
            new CMD_Home(command);
            new InvseeCommand(command);
            new PlotCommand(command);

            //Economy-System
            new CMD_Transfer(command);

            //SpecialItems
            new CMD_Disenchant(command);
            new CMD_SpecialItem(command);

            //AdventCalendar
            new CMD_AdventsKalender(command);
        });
    }

    @Override
    public void onDisable() {
        if (this.mongoClient != null) this.mongoClient.close();
        if (this.plotHandler != null) this.plotHandler.terminate();

        this.eventManager.stop();
    }
}
