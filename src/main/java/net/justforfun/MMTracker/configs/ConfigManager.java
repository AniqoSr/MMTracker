package net.justforfun.MMTracker.configs;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import net.justforfun.MMTracker.Main;

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
        this.language = this.config.getString("language", "en");
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
        this.language = this.config.getString("language", "en");
    }

    public List<ResetSchedule> getResetSchedules() {
        ArrayList<ResetSchedule> schedules = new ArrayList<>();
        ConfigurationSection section = this.config.getConfigurationSection("jadwal-reset");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (!section.getBoolean(key + ".enabled", false)) continue;
                String id = section.getString(key + ".resource.id", "");
                String mob = section.getString(key + ".resource.mob", "");
                String type = section.getString(key + ".type");
                String interval = section.getString(key + ".resource.interval");
                String dayOfWeekStr = section.getString(key + ".resource.hari");
                DayOfWeek dayOfWeek = dayOfWeekStr.equals("-1") ? null : DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());
                String timeStr = section.getString(key + ".resource.waktu");
                LocalTime time = timeStr.equals("-1") ? null : LocalTime.parse(timeStr);
                String smh = section.getString(key + ".resource.s-m-h");
                boolean respawn = section.getBoolean(key + ".resource.respawn");
                String loc = section.getString(key + ".resource.loc");
                schedules.add(new ResetSchedule(type, id, mob, dayOfWeek, time, interval, smh, respawn, loc));
            }
        }
        return schedules;
    }

    public List<String> getMobNames(String id) {
        List<String> mobNames = new ArrayList<>();
        ConfigurationSection section = this.config.getConfigurationSection("mythicmobs.spesifik_mob." + id);
        if (section != null) {
            if (section.contains("nama_mobs")) {
                mobNames.addAll(section.getStringList("nama_mobs"));
            } else if (section.contains("nama_mob")) {
                mobNames.addAll(section.getStringList("nama_mob"));
            }
        }
        return mobNames;
    }

    public int getMaxDeaths(String id, String mobName) {
        ConfigurationSection section = this.config.getConfigurationSection("mythicmobs.spesifik_mob." + id);
        if (section != null) {
            if (section.contains("nama_mobs")) {
                List<String> mobNames = section.getStringList("nama_mobs");
                List<String> maks = section.getStringList("maks");
                int index = mobNames.indexOf(mobName);
                if (index != -1 && index < maks.size()) {
                    return Integer.parseInt(maks.get(index));
                }
            } else if (section.contains("nama_mob")) {
                List<String> mobNames = section.getStringList("nama_mob");
                List<String> mak = section.getStringList("mak");
                int index = mobNames.indexOf(mobName);
                if (index != -1 && index < mak.size()) {
                    return Integer.parseInt(mak.get(index));
                }
            }
        }
        return -1; // Return -1 if no maximum death count is found
    }

    public static class ResetSchedule {
        private final String type;
        private final String id;
        private final String mob;
        private final DayOfWeek dayOfWeek;
        private final LocalTime time;
        private final String interval;
        private final String smh;
        private final boolean respawn;
        private final String loc;

        public ResetSchedule(String type, String id, String mob, DayOfWeek dayOfWeek, LocalTime time, String interval, String smh, boolean respawn, String loc) {
            this.type = type;
            this.id = id;
            this.mob = mob;
            this.dayOfWeek = dayOfWeek;
            this.time = time;
            this.interval = interval;
            this.smh = smh;
            this.respawn = respawn;
            this.loc = loc;
        }

        public String getType() {
            return this.type;
        }

        public String getId() {
            return this.id;
        }

        public String getMob() {
            return this.mob;
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

        public String getSmh() {
            return this.smh;
        }

        public boolean isRespawn() {
            return this.respawn;
        }

        public String getLoc() {
            return this.loc;
        }
    }
}
