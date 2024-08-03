package de.wintervillage.main.plot.database;

import de.wintervillage.main.plot.Plot;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IPlotDatabase {

    CompletableFuture<Void> insertAsync(Plot plot);

    CompletableFuture<Void> deleteAsync(String uniqueId);

    CompletableFuture<Void> replaceAsync(String uniqueId, Plot plot);

    CompletableFuture<Plot> findAsync(String uniqueId);

    CompletableFuture<List<Plot>> findByOwnerAsync(UUID owner);

    CompletableFuture<List<Plot>> findAsync();
}
