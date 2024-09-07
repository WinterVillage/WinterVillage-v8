package de.wintervillage.main.shop.database;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import de.wintervillage.common.core.database.SubscriberHelpers;
import de.wintervillage.common.core.database.exception.EntryNotFoundException;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import de.wintervillage.main.shop.impl.ShopImpl;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static de.wintervillage.common.core.database.UUIDConverter.toBinary;

public class ShopDatabase {

    private final MongoCollection<ShopImpl> collection;

    @Inject
    public ShopDatabase() {
        WinterVillage winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.collection = winterVillage.mongoDatabase.getCollection("shops", ShopImpl.class);
    }

    public CompletableFuture<Void> insert(Shop shop) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        this.collection.replaceOne(
                        Filters.eq("_id", toBinary(shop.uniqueId())),
                        ((ShopImpl) shop),
                        new ReplaceOptions().upsert(true)
                )
                .subscribe(new SubscriberHelpers.OperationSubscriber<>() {
                    @Override
                    public void onComplete() {
                        future.complete(null);
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
        return future;
    }

    public CompletableFuture<Void> delete(UUID uniqueId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.collection.deleteOne(
                        Filters.eq("_id", toBinary(uniqueId))
                )
                .subscribe(new SubscriberHelpers.OperationSubscriber<>() {
                    @Override
                    public void onComplete() {
                        future.complete(null);
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onNext(DeleteResult deleteResult) {
                        if (deleteResult.getDeletedCount() == 0)
                            future.completeExceptionally(new EntryNotFoundException("No result found"));
                    }
                });
        return future;
    }

    public CompletableFuture<Shop> modify(UUID uniqueId, Consumer<Shop> consumer) {
        return this.shop(uniqueId)
                .thenCompose(shop -> {
                    consumer.accept(shop);
                    return this.insert(shop).thenApply(v -> shop);
                })
                .exceptionallyCompose(throwable -> CompletableFuture.failedFuture(new RuntimeException(throwable)));
    }

    public CompletableFuture<Shop> shop(UUID uniqueId) {
        CompletableFuture<Shop> future = new CompletableFuture<>();

        this.collection.find(
                        Filters.eq("_id", toBinary(uniqueId))
                )
                .first()
                .subscribe(new SubscriberHelpers.OperationSubscriber<Shop>() {
                    @Override
                    public void onNext(Shop shop) {
                        future.complete(shop);
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

    public CompletableFuture<List<Shop>> byOwner(UUID owner) {
        List<Shop> plots = new ArrayList<>();
        CompletableFuture<List<Shop>> future = new CompletableFuture<>();

        this.collection.find(
                        Filters.eq("owner", toBinary(owner))
                )
                .subscribe(new SubscriberHelpers.OperationSubscriber<Shop>() {
                    @Override
                    public void onNext(Shop shop) {
                        plots.add(shop);
                    }

                    @Override
                    public void onComplete() {
                        if (plots.isEmpty())
                            future.completeExceptionally(new EntryNotFoundException("No results found"));
                        else future.complete(plots);
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
        return future;
    }

    public CompletableFuture<List<Shop>> find() {
        List<Shop> plots = new ArrayList<>();
        CompletableFuture<List<Shop>> future = new CompletableFuture<>();

        this.collection.find()
                .subscribe(new SubscriberHelpers.OperationSubscriber<Shop>() {
                    @Override
                    public void onNext(Shop shop) {
                        plots.add(shop);
                    }

                    @Override
                    public void onComplete() {
                        if (plots.isEmpty())
                            future.completeExceptionally(new EntryNotFoundException("No results found"));
                        else future.complete(plots);
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
        return future;
    }
}
