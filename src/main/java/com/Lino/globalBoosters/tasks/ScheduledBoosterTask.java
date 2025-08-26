package com.Lino.globalBoosters.tasks;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import com.Lino.globalBoosters.config.ConfigManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

public class ScheduledBoosterTask extends BukkitRunnable {

    private final GlobalBoosters plugin;
    private final Map<String, Long> lastActivated;

    public ScheduledBoosterTask(GlobalBoosters plugin) {
        this.plugin = plugin;
        this.lastActivated = new HashMap<>();
    }

    @Override
    public void run() {
        if (!plugin.getConfigManager().isScheduledBoostersEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();

        for (ConfigManager.ScheduledBooster schedule : plugin.getConfigManager().getScheduledBoosters()) {
            if (!schedule.getDays().contains(currentDay)) {
                continue;
            }

            if (currentHour != schedule.getHour() || currentMinute != schedule.getMinute()) {
                continue;
            }

            String scheduleKey = generateScheduleKey(schedule);
            long currentTime = System.currentTimeMillis();
            Long lastActivation = lastActivated.get(scheduleKey);

            if (lastActivation != null && (currentTime - lastActivation) < 60000) {
                continue;
            }

            if (!plugin.getBoosterManager().isBoosterActive(schedule.getType())) {
                boolean activated = plugin.getBoosterManager().activateScheduledBooster(
                        schedule.getType(),
                        schedule.getActivatorName(),
                        schedule.getDuration()
                );

                if (activated) {
                    lastActivated.put(scheduleKey, currentTime);
                }
            }
        }
    }

    private String generateScheduleKey(ConfigManager.ScheduledBooster schedule) {
        return schedule.getType().name() + "_" + schedule.getHour() + "_" + schedule.getMinute();
    }
}