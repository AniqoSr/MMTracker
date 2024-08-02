package net.justforfun.MMTracker.tasks;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.configs.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SchedulerTask extends BukkitRunnable {
    private final Main plugin;

    public SchedulerTask(Main plugin) {
        this.plugin = plugin;
    }

    public void run() {
        List<ConfigManager.ResetSchedule> schedules = this.plugin.getConfigManager().getResetSchedules();
        for (final ConfigManager.ResetSchedule schedule : schedules) {
            if (schedule.getMobId().isEmpty()) continue;
            LocalDateTime nextReset = this.getNextResetTime(schedule.getDayOfWeek(), schedule.getTime(), schedule.getInterval());
            long delay = Duration.between(LocalDateTime.now(), nextReset).getSeconds() * 20L;
            new BukkitRunnable(){

                public void run() {
                    SchedulerTask.this.resetMobData(schedule.getMobId());
                }
            }.runTaskLater(this.plugin, delay);
        }
    }

    private LocalDateTime getNextResetTime(DayOfWeek dayOfWeek, LocalTime time, String interval) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReset = now;

        if (dayOfWeek != null && time != null) {
            nextReset = now.with(TemporalAdjusters.nextOrSame(dayOfWeek)).with(time);
        }

        if (interval.equals("monthly") && nextReset.isBefore(now)) {
            nextReset = nextReset.plusMonths(1L);
        } else if (interval.equals("weekly") && nextReset.isBefore(now)) {
            nextReset = nextReset.plusWeeks(1L);
        } else if (interval.equals("daily") && nextReset.isBefore(now)) {
            nextReset = nextReset.plusDays(1L);
        } else if (interval.equals("seconds")) {
            nextReset = now.plusSeconds(parseDuration(nextReset.toString()));
        } else if (interval.equals("minutes")) {
            nextReset = now.plusMinutes(parseDuration(nextReset.toString()));
        } else if (interval.equals("hours")) {
            nextReset = now.plusHours(parseDuration(nextReset.toString()));
        }

        return nextReset;
    }

    private long parseDuration(String duration) {
        try {
            return Long.parseLong(duration.replaceAll("\\D+",""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void resetMobData(String mobName) {
        Bukkit.getLogger().info("Resetting MMTracker data for mob: " + mobName);
        this.plugin.getDatabase().resetTopDamageForMob(mobName);
        this.plugin.getDatabase().resetDeathCountForMob(mobName);
    }
}
