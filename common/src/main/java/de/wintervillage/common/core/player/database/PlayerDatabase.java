package de.wintervillage.common.core.player.database;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import de.wintervillage.common.core.database.SubscriberHelpers;
import de.wintervillage.common.core.database.exception.EntryNotFoundException;
import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.core.player.impl.WinterVillagePlayerImpl;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PlayerDatabase {

    private final MongoCollection<WinterVillagePlayerImpl> collection;

    @Inject
    public PlayerDatabase(MongoDatabase mongoDatabase) {
        this.collection = mongoDatabase.getCollection("players", WinterVillagePlayerImpl.class);
    }

    public CompletableFuture<Void> insert(WinterVillagePlayer player) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        this.collection.replaceOne(
                        Filters.eq("_id", player.uniqueId().toString()),
                        (WinterVillagePlayerImpl) player,
                        new ReplaceOptions().upsert(true)
                )
                .subscribe(new SubscriberHelpers.OperationSubscriber<>() {
                    @Override
                    public void onComplete() {
                        future.complete(null);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }
                });

        return future;
    }

    public CompletableFuture<WinterVillagePlayer> player(UUID uniqueId) {
        CompletableFuture<WinterVillagePlayer> future = new CompletableFuture<>();

        this.collection.find(Filters.eq("_id", uniqueId.toString()))
                .first()
                .subscribe(new SubscriberHelpers.OperationSubscriber<WinterVillagePlayerImpl>() {
                    @Override
                    public void onNext(WinterVillagePlayerImpl player) {
                        future.complete(player);
                    }

                    @Override
                    public void onComplete() {
                        if (!future.isDone())
                            future.completeExceptionally(new EntryNotFoundException("No result found"));
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
        return future;
    }

    public CompletableFuture<Void> modify(UUID uniqueId, Consumer<WinterVillagePlayer> consumer) {
        return this.player(uniqueId)
                .thenCompose(player -> {
                    consumer.accept(player);
                    return this.insert(player);
                })
                .exceptionallyCompose(throwable -> {
                    if (throwable instanceof EntryNotFoundException) {
                        WinterVillagePlayer player = new WinterVillagePlayerImpl(uniqueId);
                        consumer.accept(player);
                        return this.insert(player);
                    }
                    throw new RuntimeException(throwable);
                });
    }
}
