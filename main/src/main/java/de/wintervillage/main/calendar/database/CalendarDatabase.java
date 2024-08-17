package de.wintervillage.main.calendar.database;

import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import de.wintervillage.common.core.database.SubscriberHelpers;
import de.wintervillage.common.core.database.exception.EntryNotFoundException;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.calendar.CalendarDay;
import de.wintervillage.main.calendar.impl.CalendarDayImpl;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CalendarDatabase {

    private final WinterVillage winterVillage;

    private final MongoCollection<CalendarDayImpl> collection;

    @Inject
    public CalendarDatabase() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.collection = this.winterVillage.mongoDatabase.getCollection("calendar", CalendarDayImpl.class);
    }

    public CompletableFuture<Void> insert(CalendarDay calendarDay) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (calendarDay.day() < 1 || calendarDay.day() > 24) {
            future.completeExceptionally(new IllegalArgumentException("Day must be between 1 and 24"));
            return future;
        }

        this.collection.replaceOne(
                Filters.eq("day", calendarDay.day()),
                ((CalendarDayImpl) calendarDay),
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

    public CompletableFuture<Void> delete(int day) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        this.collection.deleteOne(
                        Filters.eq("day", day)
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
                            future.completeExceptionally(new EntryNotFoundException("Result not found"));
                    }
                });
        return future;
    }

    public CompletableFuture<CalendarDay> find(int day) {
        CompletableFuture<CalendarDay> future = new CompletableFuture<>();

        this.collection.find(
                        Filters.eq("day", day)
                )
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

    public CompletableFuture<Void> modify(int day, Consumer<CalendarDay> consumer) {
        return this.find(day)
                .thenCompose(calendarDay -> {
                    consumer.accept(calendarDay);
                    return this.insert(calendarDay);
                })
                .exceptionallyCompose(throwable -> {
                    throw new RuntimeException(throwable);
                });
    }

    public CompletableFuture<List<CalendarDay>> find() {
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
