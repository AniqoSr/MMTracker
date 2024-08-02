package net.justforfun.MMTracker.tasks;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.configs.ConfigManager;
import net.justforfun.MMTracker.storage.Database;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class SchedulerTask extends BukkitRunnable {
    private final Main plugin;
    private final Database database;
    private final ConfigManager configManager;
    private final Map<String, LocalDateTime> nextResetTimes;

    public SchedulerTask(Main plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
        this.configManager = plugin.getConfigManager();
        this.nextResetTimes = new HashMap<>();
        this.loadResetTimes();
    }

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        Iterator<Map.Entry<String, LocalDateTime>> iterator = this.nextResetTimes.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, LocalDateTime> entry = iterator.next();
            if (now.isAfter(entry.getValue())) {
                ConfigManager.ResetSchedule schedule = this.configManager.getResetSchedules().stream()
                        .filter(s -> s.getType().equals("id") ? s.getId().equals(entry.getKey()) : s.getMob().equals(entry.getKey()))
                        .findFirst()
                        .orElse(null);
                if (schedule != null) {
                    this.resetMobData(schedule);
                    this.nextResetTimes.put(entry.getKey(), this.calculateNextReset(schedule, now));
                }
            }
        }
    }

    private void loadResetTimes() {
        for (ConfigManager.ResetSchedule schedule : this.configManager.getResetSchedules()) {
            String key = schedule.getType().equals("id") ? schedule.getId() : schedule.getMob();
            this.nextResetTimes.put(key, this.calculateNextReset(schedule, LocalDateTime.now()));
        }
    }

    private LocalDateTime calculateNextReset(ConfigManager.ResetSchedule schedule, LocalDateTime now) {
        LocalDateTime nextReset = now;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        switch (schedule.getInterval().toLowerCase()) {
            case "harian":
                nextReset = now.with(LocalTime.parse(schedule.getTime().toString(), timeFormatter));
                if (nextReset.isBefore(now)) {
                    nextReset = nextReset.plusDays(1);
                }
                break;
            case "mingguan":
                DayOfWeek dayOfWeek = schedule.getDayOfWeek();
                nextReset = now.with(TemporalAdjusters.nextOrSame(dayOfWeek)).with(LocalTime.parse(schedule.getTime().toString(), timeFormatter));
                if (nextReset.isBefore(now)) {
                    nextReset = nextReset.plusWeeks(1);
                }
                break;
            case "bulanan":
                nextReset = now.with(LocalTime.parse(schedule.getTime().toString(), timeFormatter));
                if (nextReset.isBefore(now)) {
                    nextReset = nextReset.plusMonths(1);
                }
                break;
            case "detik":
                nextReset = now.plusSeconds(this.parseSMH(schedule.getSmh()));
                break;
            case "jam":
                nextReset = now.plusHours(this.parseSMH(schedule.getSmh()));
                break;
            case "menit":
                nextReset = now.plusMinutes(this.parseSMH(schedule.getSmh()));
                break;
        }

        return nextReset;
    }

    private long parseSMH(String s_m_h) {
        long seconds = 0L;
        if (s_m_h.endsWith("s")) {
            seconds = Long.parseLong(s_m_h.replace("s", ""));
        } else if (s_m_h.endsWith("m")) {
            seconds = Long.parseLong(s_m_h.replace("m", "")) * 60L;
        } else if (s_m_h.endsWith("h")) {
            seconds = Long.parseLong(s_m_h.replace("h", "")) * 3600L;
        }

        return seconds;
    }

    private void resetMobData(ConfigManager.ResetSchedule schedule) {
        String mobId = schedule.getId();
        Bukkit.getLogger().info("Resetting MMTracker data for id: " + mobId);

        if (schedule.getType().equals("id")) {
            this.plugin.getDatabase().resetTopDamageForMob(mobId);

            for (String mobName : this.configManager.getMobNames(mobId)) {
                this.plugin.getDatabase().resetDeathCountForMob(mobId, mobName);
            }
        } else {
            String mobName = schedule.getMob();
            this.plugin.getDatabase().resetTopDamageForMob(mobName);
            this.plugin.getDatabase().resetDeathCountForMob(mobId, mobName);
        }

        if (schedule.isRespawn() && !schedule.getLoc().equals("none")) {
            String[] locParts = schedule.getLoc().split(":");
            String worldName = locParts[0];
            String[] coords = locParts[1].split(",");
            Location loc = new Location(Bukkit.getWorld(worldName), Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
            this.respawnMob(schedule.getType().equals("id") ? mobId : schedule.getMob(), loc);
        }
    }

    private void respawnMob(String mobName, Location loc) {
        MythicMob mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mobName).orElse(null);
        if (mythicMob != null) {
            AbstractLocation abstractLocation = BukkitAdapter.adapt(loc);
            ActiveMob activeMob = mythicMob.spawn(abstractLocation, 1.0);
            if (activeMob != null) {
                Bukkit.getLogger().info("Respawning mob " + mobName + " at location: " + loc);
            } else {
                Bukkit.getLogger().warning("Failed to respawn mob " + mobName + " at location: " + loc);
            }
        } else {
            Bukkit.getLogger().warning("Could not find MythicMob with name: " + mobName);
        }
    }
}
