package de.wintervillage.main.plot.database;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import de.wintervillage.common.core.database.SubscriberHelpers;
import de.wintervillage.common.core.database.exception.EntryNotFoundException;
import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import org.bson.Document;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlotDatabase implements IPlotDatabase {

    private final WinterVillage winterVillage;

    private final MongoCollection<Plot> collection;

    @Inject
    public PlotDatabase() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.collection = this.winterVillage.mongoDatabase.getCollection("plots", Plot.class);
    }

    @Override
    public CompletableFuture<Void> insertAsync(Plot plot) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.collection.insertOne(plot).subscribe(new SubscriberHelpers.OperationSubscriber<>() {
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

    @Override
    public CompletableFuture<Void> deleteAsync(String uniqueId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.collection.deleteOne(Filters.eq("_id", uniqueId))
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

    @Override
    public CompletableFuture<Void> replaceAsync(String uniqueId, Plot plot) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.collection.replaceOne(Filters.eq("_id", uniqueId), plot)
                .subscribe(new SubscriberHelpers.OperationSubscriber<>() {
                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onNext(UpdateResult updateResult) {
                        if (updateResult.getModifiedCount() == 0)
                            future.completeExceptionally(new EntryNotFoundException("No result found"));
                        else future.complete(null);
                    }
                });
        return future;
    }

    public CompletableFuture<Void> updateOwner(String uniqueId, UUID owner) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.collection.updateOne(Filters.eq("_id", uniqueId),
                        new Document("$set", new Document("owner", owner.toString())))
                .subscribe(new SubscriberHelpers.OperationSubscriber<>() {
                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onNext(UpdateResult updateResult) {
                        if (updateResult.getModifiedCount() == 0)
                            future.completeExceptionally(new EntryNotFoundException("No result found"));
                        else future.complete(null);
                    }
                });
        return future;
    }

    public CompletableFuture<Void> updateBoundingBox(String uniqueId, BoundingBox2D boundingBox) {
        Document boundingBoxDocument = new Document();
        boundingBoxDocument.append("minX", boundingBox.getMinX());
        boundingBoxDocument.append("minZ", boundingBox.getMinZ());
        boundingBoxDocument.append("maxX", boundingBox.getMaxX());
        boundingBoxDocument.append("maxZ", boundingBox.getMaxZ());

        CompletableFuture<Void> future = new CompletableFuture<>();
        this.collection.updateOne(Filters.eq("_id", uniqueId),
                        new Document("$set", new Document("boundingBox", boundingBoxDocument)))
                .subscribe(new SubscriberHelpers.OperationSubscriber<>() {
                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onNext(UpdateResult updateResult) {
                        if (updateResult.getModifiedCount() == 0)
                            future.completeExceptionally(new EntryNotFoundException("No result found"));
                        else future.complete(null);
                    }
                });
        return future;
    }

    public CompletableFuture<Void> updateMembers(String uniqueId, List<UUID> members) {
        List<String> memberStrings = members.stream()
                .map(UUID::toString)
                .toList();

        CompletableFuture<Void> future = new CompletableFuture<>();
        this.collection.updateOne(Filters.eq("_id", uniqueId),
                        new Document("$set", new Document("members", memberStrings)))
                .subscribe(new SubscriberHelpers.OperationSubscriber<>() {
                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onNext(UpdateResult updateResult) {
                        if (updateResult.getModifiedCount() == 0)
                            future.completeExceptionally(new EntryNotFoundException("No result found"));
                        else future.complete(null);
                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<Plot> findAsync(String uniqueId) {
        CompletableFuture<Plot> future = new CompletableFuture<>();
        this.collection.find(Filters.eq("_id", uniqueId))
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

    @Override
    public CompletableFuture<List<Plot>> findByOwnerAsync(UUID owner) {
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

    @Override
    public CompletableFuture<List<Plot>> findAsync() {
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
