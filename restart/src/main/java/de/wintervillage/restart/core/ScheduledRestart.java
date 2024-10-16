package de.wintervillage.restart.core;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ScheduledRestart {

    private final DayOfWeek dayOfWeek;
    private final LocalTime restartTime;

    private final List<Integer> notifyBefore;
    private final Set<Integer> notified;

    public ScheduledRestart(String dayOfWeek, String restartTime, List<Integer> notifyBefore) {
        this.dayOfWeek = DayOfWeek.valueOf(dayOfWeek.toUpperCase(Locale.ENGLISH));
        this.restartTime = LocalTime.parse(restartTime, DateTimeFormatter.ofPattern("HH:mm:ss"));

        this.notifyBefore = notifyBefore;
        this.notified = new HashSet<>();
    }

    public void checkAndNotify(LocalDateTime now, RestartManagement.NotifyCallback callback) {
        if (now.getDayOfWeek() != this.dayOfWeek) return;

        for (int secondsBefore : this.notifyBefore) {
            LocalTime notifyTime = this.restartTime.minusSeconds(secondsBefore);

            if (now.toLocalTime().equals(notifyTime) && !this.notified.contains(secondsBefore)) {
                callback.notify(secondsBefore);
                this.notified.add(secondsBefore);
            }
        }
    }

    public boolean shouldRestart(LocalDateTime now) {
        return now.getDayOfWeek() == this.dayOfWeek && now.toLocalTime().equals(this.restartTime);
    }
}
