package de.wintervillage.main.plot.database;

import de.wintervillage.main.plot.Plot;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IPlotDatabase {

    void insertSync(Plot plot);

    CompletableFuture<Void> insertAsync(Plot plot);

    void deleteSync(String uniqueId);

    CompletableFuture<Void> deleteAsync(String uniqueId);

    void updateSync(String uniqueId, Plot plot);

    CompletableFuture<Void> updateAsync(String uniqueId, Plot plot);

    Plot findSync(String uniqueId);

    CompletableFuture<Plot> findAsync(String uniqueId);

    List<Plot> findByOwnerSync(UUID owner);

    CompletableFuture<List<Plot>> findByOwnerAsync(UUID owner);

    List<Plot> findSync();

    CompletableFuture<List<Plot>> findAsync();
}
