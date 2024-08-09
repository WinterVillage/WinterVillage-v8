package de.wintervillage.main.plot.database;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import de.wintervillage.common.core.database.SubscriberHelpers;
import de.wintervillage.common.core.database.exception.EntryNotFoundException;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import de.wintervillage.main.plot.impl.PlotImpl;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PlotDatabase {

    private final WinterVillage winterVillage;
    private final MongoCollection<PlotImpl> collection;

    @Inject
    public PlotDatabase() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.collection = this.winterVillage.mongoDatabase.getCollection("plots", PlotImpl.class);
    }

    public CompletableFuture<Void> insert(Plot plot) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        this.collection.replaceOne(
                        Filters.eq("_id", plot.uniqueId().toString()),
                        ((PlotImpl) plot),
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
        this.collection.deleteOne(Filters.eq("_id", uniqueId.toString()))
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

    public CompletableFuture<Void> modify(UUID uniqueId, Consumer<Plot> consumer) {
        return this.plot(uniqueId)
                .thenCompose(plot -> {
                    consumer.accept(plot);
                    return this.insert(plot);
                })
                .exceptionallyCompose(throwable -> {
                    throw new RuntimeException(throwable);
                });
    }

    public CompletableFuture<Plot> plot(UUID uniqueId) {
        CompletableFuture<Plot> future = new CompletableFuture<>();

        this.collection.find(Filters.eq("_id", uniqueId.toString()))
                .first()
                .subscribe(new SubscriberHelpers.OperationSubscriber<Plot>() {
                    @Override
                    public void onNext(Plot plot) {
                        future.complete(plot);
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

    public CompletableFuture<List<Plot>> byOwner(UUID owner) {
        List<Plot> plots = new ArrayList<>();
        CompletableFuture<List<Plot>> future = new CompletableFuture<>();

        this.collection.find(Filters.eq("owner", owner.toString()))
                .subscribe(new SubscriberHelpers.OperationSubscriber<Plot>() {
                    @Override
                    public void onNext(Plot plot) {
                        plots.add(plot);
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

    public CompletableFuture<List<Plot>> find() {
        List<Plot> plots = new ArrayList<>();
        CompletableFuture<List<Plot>> future = new CompletableFuture<>();

        this.collection.find()
                .subscribe(new SubscriberHelpers.OperationSubscriber<Plot>() {
                    @Override
                    public void onNext(Plot plot) {
                        plots.add(plot);
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
