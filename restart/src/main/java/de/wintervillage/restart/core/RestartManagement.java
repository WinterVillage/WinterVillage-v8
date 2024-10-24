package de.wintervillage.restart.core;

import de.wintervillage.common.core.config.Document;
import de.wintervillage.restart.core.config.ConfigHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RestartManagement {

    private final Collection<ScheduledRestart> scheduled = new ArrayList<>();
    private final ConfigHandler configHandler;

    public RestartManagement(ConfigHandler configHandler) {
        this.configHandler = configHandler;
        this.register();
    }

    private void register() {
        if (!this.configHandler.getConfig().contains("scheduled")) return;
        if (this.configHandler.getConfig().getArray("scheduled").isEmpty()) return;

        this.configHandler.getConfig().getArray("scheduled").forEach(entry -> {
            Document scheduledDocument = new Document(entry.getAsJsonObject());

            String dayOfWeek = scheduledDocument.getString("dayOfWeek");
            String time = scheduledDocument.getString("time");
            List<Integer> notifyBefore = new ArrayList<>();

            scheduledDocument.getArray("notifyBefore").forEach(jsonElement -> notifyBefore.add(jsonElement.getAsInt()));
            this.scheduled.add(new ScheduledRestart(dayOfWeek, time, notifyBefore));
        });
    }

    public void checkScheduledRestarts(LocalDateTime now, Runnable restartAction, NotifyCallback callback) {
        LocalDateTime current = now.withNano(0);
        this.scheduled.forEach(scheduledRestart -> {
            scheduledRestart.checkAndNotify(current, callback);
            if (scheduledRestart.shouldRestart(current)) restartAction.run();
        });
    }

    @FunctionalInterface
    public interface NotifyCallback {
        void notify(int secondsBefore);
    }
}
