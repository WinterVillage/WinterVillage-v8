package de.wintervillage.main.calendar.database;

import de.wintervillage.main.calendar.CalendarDay;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ICalendarDatabase {

    CompletableFuture<Void> insertAsync(CalendarDay calendarDay);

    CompletableFuture<Void> deleteAsync(int day);

    CompletableFuture<Void> replaceAsync(int day, CalendarDay calendarDay);

    CompletableFuture<CalendarDay> findAsync(int day);

    CompletableFuture<List<CalendarDay>> findAsync();
}
