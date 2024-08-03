package de.wintervillage.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.reactivestreams.client.MongoDatabase;
import de.wintervillage.common.core.player.database.PlayerDatabase;

public class WinterVillageModule extends AbstractModule {

    private final MongoDatabase mongoDatabase;

    public WinterVillageModule(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    protected void configure() {
        // databases
        this.bind(PlayerDatabase.class).in(Singleton.class);
    }

    @Provides
    public MongoDatabase provideDatabase() {
        return this.mongoDatabase;
    }
}
