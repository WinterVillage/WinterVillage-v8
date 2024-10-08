package de.wintervillage.main.calendar;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CalendarHandler {

    private final WinterVillage winterVillage;

    public List<CalendarDay> days;

    public final LocalDate startDate, endDate;

    @Inject
    public CalendarHandler() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.days = new ArrayList<>();

        this.startDate = LocalDate.of(2024, Month.DECEMBER, 1);
        this.endDate = LocalDate.of(2025, Month.JANUARY, 6);

        this.forceUpdate();
    }

    public void forceUpdate() {
        this.winterVillage.calendarDatabase.find()
                .thenAccept(calendarDays -> {
                    this.days.clear();
                    this.days.addAll(calendarDays);
                })
                .exceptionally(t -> {
                    this.winterVillage.getLogger().warning("Could not load calendar: " + t.getMessage());
                    return null;
                });
    }

    /**
     * Get a calendar day by the day
     * @param day {@link Integer}
     * @return {@link Optional<CalendarDay>}
     */
    public Optional<CalendarDay> byDay(int day) {
        return this.days.stream()
                .filter(calendarDay -> calendarDay.day() == day)
                .findFirst();
    }

    /**
     * Check if the current date is within the range of the 1st december and 6th january
     * @return {@link Boolean}
     */
    public boolean withinRange() {
        LocalDate now = LocalDate.now();
        return now.isAfter(this.startDate) && now.isBefore(this.endDate);
    }

    /**
     * Check if the day is obtainable
     * @param day {@link Integer}
     * @return {@link Boolean}
     */
    public boolean obtainable(int day) {
        if (day < 1 || day > 24) throw new IllegalArgumentException("Invalid day");

        if (!this.withinRange()) return false;
        LocalDate current = LocalDate.now();

        LocalDate doorDate = LocalDate.of(2024, Month.DECEMBER, day);
        return !current.isBefore(doorDate);
    }
}
