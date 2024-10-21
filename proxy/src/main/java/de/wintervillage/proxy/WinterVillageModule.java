package de.wintervillage.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.velocitypowered.api.proxy.ProxyServer;
import de.wintervillage.common.core.player.database.PlayerDatabase;
import de.wintervillage.proxy.player.PlayerHandler;

public class WinterVillageModule extends AbstractModule {

    private final WinterVillage winterVillage;

    private final ProxyServer proxyServer;

    private final MongoDatabase mongoDatabase;

    public WinterVillageModule(WinterVillage winterVillage, ProxyServer proxyServer, MongoDatabase mongoDatabase) {
        this.winterVillage = winterVillage;
        this.proxyServer = proxyServer;
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    protected void configure() {
        // databases
        this.bind(PlayerDatabase.class).in(Singleton.class);

        // handler
        this.bind(PlayerHandler.class).in(Singleton.class);
    }

    @Provides
    public WinterVillage provideWinterVillage() {
        return this.winterVillage;
    }

    @Provides
    public ProxyServer provideProxyServer() {
        return this.proxyServer;
    }

    @Provides
    public MongoDatabase provideDatabase() {
        return this.mongoDatabase;
    }
}
