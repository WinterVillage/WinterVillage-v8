package de.wintervillage.main;

import com.comphenix.protocol.ProtocolManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.reactivestreams.client.MongoDatabase;
import de.wintervillage.common.core.config.Document;
import de.wintervillage.common.core.player.database.PlayerDatabase;
import de.wintervillage.main.calendar.CalendarHandler;
import de.wintervillage.main.calendar.database.CalendarDatabase;
import de.wintervillage.main.event.EventManager;
import de.wintervillage.main.player.PlayerHandler;
import de.wintervillage.main.plot.PlotHandler;
import de.wintervillage.main.plot.database.PlotDatabase;
import de.wintervillage.main.scoreboard.ScoreboardHandler;
import de.wintervillage.main.shop.ShopHandler;
import de.wintervillage.main.shop.database.ShopDatabase;
import de.wintervillage.main.specialitems.SpecialItems;
import net.luckperms.api.LuckPerms;

public class WinterVillageModule extends AbstractModule {

    private final Document configDocument;

    private final ProtocolManager protocolManager;
    private final MongoDatabase mongoDatabase;
    private final LuckPerms luckPerms;

    public WinterVillageModule(
            Document configDocument,
            ProtocolManager protocolManager,
            MongoDatabase mongoDatabase,
            LuckPerms luckPerms
    ) {
        this.configDocument = configDocument;
        this.protocolManager = protocolManager;
        this.mongoDatabase = mongoDatabase;
        this.luckPerms = luckPerms;
    }

    @Override
    protected void configure() {
        // databases
        if (this.configDocument.getDocument("enabled").getBoolean("plot_handler"))
            this.bind(PlotDatabase.class).in(Singleton.class);
        if (this.configDocument.getDocument("enabled").getBoolean("shop_handler"))
            this.bind(ShopDatabase.class).in(Singleton.class);
        this.bind(CalendarDatabase.class).in(Singleton.class);
        this.bind(PlayerDatabase.class).in(Singleton.class);

        // managers
        if (this.configDocument.getDocument("enabled").getBoolean("plot_handler"))
            this.bind(PlotHandler.class).in(Singleton.class);
        if (this.configDocument.getDocument("enabled").getBoolean("shop_handler"))
            this.bind(ShopHandler.class).in(Singleton.class);
        this.bind(CalendarHandler.class).in(Singleton.class);
        this.bind(PlayerHandler.class).in(Singleton.class);
        this.bind(SpecialItems.class).in(Singleton.class);
        this.bind(EventManager.class).in(Singleton.class);
        this.bind(ScoreboardHandler.class).in(Singleton.class);
    }

    @Provides
    public MongoDatabase provideDatabase() {
        return this.mongoDatabase;
    }

    @Provides
    public ProtocolManager provideProtocolManager() {
        return this.protocolManager;
    }

    @Provides
    public LuckPerms provideLuckPerms() {
        return this.luckPerms;
    }
}
