package de.wintervillage.main.plot.database;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.database.SubscriberHelpers;
import de.wintervillage.main.plot.Plot;
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
    public void insertSync(Plot plot) {
        try {
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
            future.join();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
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
    public void deleteSync(String uniqueId) {
        try {
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
                            if (deleteResult.getDeletedCount() == 0) future.completeExceptionally(new RuntimeException("No result found for uniqueId: " + uniqueId));
                        }
                    });
            future.join();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
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
                        if (deleteResult.getDeletedCount() == 0) future.completeExceptionally(new RuntimeException("No result found for uniqueId: " + uniqueId));
                    }
                });
        return future;
    }

    @Override
    public void updateSync(String uniqueId, Plot plot) {
        try {
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
                                future.completeExceptionally(new RuntimeException("No result found for uniqueId: " + uniqueId));
                            else future.complete(null);
                        }
                    });
            future.join();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public CompletableFuture<Void> updateAsync(String uniqueId, Plot plot) {
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
                            future.completeExceptionally(new RuntimeException("No result found for uniqueId: " + uniqueId));
                        else future.complete(null);
                    }
                });
        return future;
    }

    @Override
    public Plot findSync(String uniqueId) {
        try {
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
                                future.completeExceptionally(new RuntimeException("No result found for uniqueId: " + uniqueId));
                        }

                        @Override
                        public void onError(Throwable t) {
                            future.completeExceptionally(t);
                        }
                    });
            return future.join();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
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
                            future.completeExceptionally(new RuntimeException("No result found for uniqueId: " + uniqueId));
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
        return future;
    }

    @Override
    public List<Plot> findByOwnerSync(UUID owner) {
        try {
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
                                future.completeExceptionally(new RuntimeException("No results found for owner uniqueId: " + owner));
                            else future.complete(plots);
                        }

                        @Override
                        public void onError(Throwable t) {
                            future.completeExceptionally(t);
                        }
                    });
            return future.join();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
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
                            future.completeExceptionally(new RuntimeException("No results found for owner uniqueId: " + owner));
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
    public List<Plot> findSync() {
        try {
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
                                future.completeExceptionally(new RuntimeException("No results found"));
                            else future.complete(plots);
                        }

                        @Override
                        public void onError(Throwable t) {
                            future.completeExceptionally(t);
                        }
                    });
            return future.join();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
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
                            future.completeExceptionally(new RuntimeException("No results found"));
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
