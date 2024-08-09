package de.wintervillage.main;

import com.comphenix.protocol.ProtocolManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.reactivestreams.client.MongoDatabase;
import de.wintervillage.common.core.player.database.PlayerDatabase;
import de.wintervillage.common.paper.player.PlayerHandler;
import de.wintervillage.main.calendar.CalendarHandler;
import de.wintervillage.main.calendar.database.CalendarDatabase;
import de.wintervillage.main.economy.EconomyManager;
import de.wintervillage.main.economy.shop.ShopManager;
import de.wintervillage.main.event.EventManager;
import de.wintervillage.main.plot.PlotHandler;
import de.wintervillage.main.plot.database.PlotDatabase;
import de.wintervillage.main.specialitems.SpecialItems;
import org.bukkit.plugin.java.JavaPlugin;

public class WinterVillageModule extends AbstractModule {

    private final JavaPlugin javaPlugin;
    private final ProtocolManager protocolManager;
    private final MongoDatabase mongoDatabase;

    public WinterVillageModule(JavaPlugin javaPlugin, ProtocolManager protocolManager, MongoDatabase mongoDatabase) {
        this.javaPlugin = javaPlugin;
        this.protocolManager = protocolManager;
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    protected void configure() {
        // databases
        this.bind(PlotDatabase.class).in(Singleton.class);
        this.bind(CalendarDatabase.class).in(Singleton.class);
        this.bind(PlayerDatabase.class).in(Singleton.class);

        // managers
        this.bind(PlotHandler.class).in(Singleton.class);
        this.bind(CalendarHandler.class).in(Singleton.class);
        this.bind(PlayerHandler.class).in(Singleton.class);
        this.bind(ShopManager.class).in(Singleton.class);
        this.bind(EconomyManager.class).in(Singleton.class);
        this.bind(SpecialItems.class).in(Singleton.class);
        this.bind(EventManager.class).in(Singleton.class);
    }

    @Provides
    public MongoDatabase provideDatabase() {
        return this.mongoDatabase;
    }

    @Provides
    public JavaPlugin provideJavaPlugin() {
        return this.javaPlugin;
    }

    @Provides
    public ProtocolManager provideProtocolManager() {
        return this.protocolManager;
    }
}
