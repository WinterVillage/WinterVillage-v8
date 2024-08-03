package de.wintervillage.main.calendar.database;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import de.wintervillage.common.core.database.SubscriberHelpers;
import de.wintervillage.common.core.database.exception.EntryNotFoundException;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.calendar.CalendarDay;
import org.bson.Document;
import org.bson.types.Binary;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CalendarDatabase implements ICalendarDatabase {

    private final WinterVillage winterVillage;

    private final MongoCollection<CalendarDay> collection;

    @Inject
    public CalendarDatabase() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.collection = this.winterVillage.mongoDatabase.getCollection("calendar", CalendarDay.class);
    }

    @Override
    public CompletableFuture<Void> insertAsync(CalendarDay calendarDay) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (calendarDay.getDay() < 1 || calendarDay.getDay() > 24) {
            future.completeExceptionally(new IllegalArgumentException("Day must be between 1 and 24"));
            return future;
        }

        this.collection.replaceOne(
                Filters.eq("day", calendarDay.getDay()),
                calendarDay,
                new ReplaceOptions().upsert(true)
        ).subscribe(new SubscriberHelpers.OperationSubscriber<>() {
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
    public CompletableFuture<Void> deleteAsync(int day) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.collection.deleteOne(Filters.eq("day", day))
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
                            future.completeExceptionally(new EntryNotFoundException("Calendar day not found"));
                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<Void> replaceAsync(int day, CalendarDay calendarDay) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.collection.replaceOne(Filters.eq("day", day), calendarDay)
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

    public CompletableFuture<Void> updateItemStack(int day, ItemStack itemStack) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.collection.updateOne(Filters.eq("day", day),
                        new Document("$set", new Document("itemStack", new Binary(itemStack.serializeAsBytes()))))
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

    public CompletableFuture<Void> updateOpened(int day, List<UUID> opened) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        List<String> openedStrings = opened.stream()
                .map(UUID::toString)
                .toList();

        this.collection.updateOne(Filters.eq("day", day),
                        new Document("$set", new Document("opened", openedStrings)))
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
    public CompletableFuture<CalendarDay> findAsync(int day) {
        CompletableFuture<CalendarDay> future = new CompletableFuture<>();
        this.collection.find(Filters.eq("day", day))
                .first()
                .subscribe(new SubscriberHelpers.OperationSubscriber<CalendarDay>() {
                    @Override
                    public void onNext(CalendarDay calendarDay) {
                        future.complete(calendarDay);
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
    public CompletableFuture<List<CalendarDay>> findAsync() {
        List<CalendarDay> calendarDays = new ArrayList<>();

        CompletableFuture<List<CalendarDay>> future = new CompletableFuture<>();
        this.collection.find()
                .subscribe(new SubscriberHelpers.OperationSubscriber<CalendarDay>() {
                    @Override
                    public void onNext(CalendarDay calendarDay) {
                        calendarDays.add(calendarDay);
                    }

                    @Override
                    public void onComplete() {
                        if (calendarDays.isEmpty())
                            future.completeExceptionally(new EntryNotFoundException("No results found"));
                        else future.complete(calendarDays);
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
        return future;
    }
}
