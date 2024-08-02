package net.justforfun.MMTracker.configs;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.justforfun.MMTracker.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final Main plugin;
    private FileConfiguration config;
    private String language;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    public void loadConfig() {
        this.plugin.saveDefaultConfig();
        this.config = this.plugin.getConfig();
        this.language = this.config.getString("language", "id");
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public boolean isDebug() {
        return this.config.getBoolean("debug", false);
    }

    public void reloadConfig() {
        this.plugin.reloadConfig();
        this.config = this.plugin.getConfig();
        this.language = this.config.getString("language", "id");
    }

    public List<ResetSchedule> getResetSchedules() {
        List<ResetSchedule> schedules = new ArrayList<>();
        ConfigurationSection section = this.config.getConfigurationSection("jadwal-reset");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (!section.getBoolean(key + ".enabled", false)) continue;
                String mobId = section.getString(key + ".id");
                String dayString = section.getString(key + ".hari");
                DayOfWeek dayOfWeek = dayString.equals("false") ? null : translateDay(dayString);
                String timeString = section.getString(key + ".waktu");
                LocalTime time = timeString.equals("false") ? null : LocalTime.parse(timeString);
                String interval = translateInterval(section.getString(key + ".interval"));
                schedules.add(new ResetSchedule(mobId, dayOfWeek, time, interval));
            }
        }
        return schedules;
    }

    private DayOfWeek translateDay(String day) {
        switch (day.toLowerCase()) {
            case "senin": return DayOfWeek.MONDAY;
            case "selasa": return DayOfWeek.TUESDAY;
            case "rabu": return DayOfWeek.WEDNESDAY;
            case "kamis": return DayOfWeek.THURSDAY;
            case "jumat": return DayOfWeek.FRIDAY;
            case "sabtu": return DayOfWeek.SATURDAY;
            case "minggu": return DayOfWeek.SUNDAY;
            default: return null; // Handle invalid day appropriately
        }
    }

    private String translateInterval(String interval) {
        switch (interval.toLowerCase()) {
            case "harian": return "daily";
            case "mingguan": return "weekly";
            case "bulanan": return "monthly";
            case "detik": return "seconds";
            case "jam": return "hours";
            case "menit": return "minutes";
            default: return interval.toLowerCase();
        }
    }

    public int getMaxDeaths(String mobName) {
        List<Map<?, ?>> mobs = this.config.getMapList("mythicmobs.spesifik_mob");
        for (Map<?, ?> mob : mobs) {
            if (mob.containsKey("nama_mobs")) {
                Map<String, Object> namaMobs = (Map<String, Object>) mob.get("nama_mobs");
                for (String key : namaMobs.keySet()) {
                    if (mobName.equals(namaMobs.get(key))) {
                        Map<String, Object> maksMap = (Map<String, Object>) mob.get("maks");
                        if (maksMap != null && maksMap.containsKey(key)) {
                            Object maxValue = maksMap.get(key);
                            return parseMaxValue(maxValue);
                        }
                    }
                }
            } else if (mob.containsKey("nama_mob") && mobName.equals(mob.get("nama_mob"))) {
                Object maxValue = mob.get("mak");
                return parseMaxValue(maxValue);
            }
        }
        return -1; // Return -1 if no maximum death count is found
    }

    private int parseMaxValue(Object maxValue) {
        if (maxValue instanceof Number) {
            return ((Number) maxValue).intValue();
        } else if (maxValue instanceof String) {
            try {
                return Integer.parseInt((String) maxValue);
            } catch (NumberFormatException e) {
                return -1; // Return -1 if parsing fails
            }
        }
        return -1; // Return -1 if value is neither a number nor a string
    }

    public static class ResetSchedule {
        private final String mobId;
        private final DayOfWeek dayOfWeek;
        private final LocalTime time;
        private final String interval;

        public ResetSchedule(String mobId, DayOfWeek dayOfWeek, LocalTime time, String interval) {
            this.mobId = mobId;
            this.dayOfWeek = dayOfWeek;
            this.time = time;
            this.interval = interval;
        }

        public String getMobId() {
            return this.mobId;
        }

        public DayOfWeek getDayOfWeek() {
            return this.dayOfWeek;
        }

        public LocalTime getTime() {
            return this.time;
        }

        public String getInterval() {
            return this.interval;
        }
    }
}
