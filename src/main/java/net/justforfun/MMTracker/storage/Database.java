package net.justforfun.MMTracker.storage;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.configs.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Database {
    private final Main plugin;
    private final ConfigManager configManager;
    private final FileConfiguration yamlConfig;
    private final File yamlFile;
    private final boolean debug;

    public Database(Main plugin, ConfigManager configManager, FileConfiguration yamlConfig, File yamlFile) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.yamlConfig = yamlConfig;
        this.yamlFile = yamlFile;
        this.debug = configManager.isDebug();
    }

    public FileConfiguration getYamlConfig() {
        return this.yamlConfig;
    }

    public void saveYamlConfig() {
        try {
            this.yamlConfig.save(this.yamlFile);
            if (this.debug) {
                this.plugin.getLogger().info("YAML configuration saved.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getDeathCount(String mobId, String mobName) {
        return this.yamlConfig.getInt("id." + mobId + ".mobs." + mobName + ".players.1.deaths", 0);
    }

    public void increaseDeathCount(String mobId, String mobName) {
        String path = "id." + mobId + ".mobs." + mobName + ".players.1.deaths";
        int currentDeathCount = this.yamlConfig.getInt(path, 0);
        this.yamlConfig.set(path, currentDeathCount + 1);
        if (this.debug) {
            this.plugin.getLogger().info("Death count increased for " + mobName + " to " + (currentDeathCount + 1));
        }
        this.saveYamlConfig();
    }

    public void updateStorage(String mobId, String mobName, String playerName, int damage) {
        String path = "id." + mobId + ".mobs." + mobName + ".players.1.";
        int currentDamage = this.yamlConfig.getInt(path + "Damage", 0);
        this.yamlConfig.set(path + "Name", playerName);
        this.yamlConfig.set(path + "Damage", currentDamage + damage);

        String totalPath = "id." + mobId + ".totaldamage.players.1.";
        int totalDamage = this.yamlConfig.getInt(totalPath + "Damage", 0);
        this.yamlConfig.set(totalPath + "Name", playerName);
        this.yamlConfig.set(totalPath + "Damage", totalDamage + damage);

        if (this.debug) {
            this.plugin.getLogger().info("Updated damage for player=" + playerName + ", mobName=" + mobName + ", damage=" + (currentDamage + damage));
        }
        this.saveYamlConfig();
    }

    public void resetTopDamageForMob(String mobId) {
        this.yamlConfig.set("id." + mobId, null);
        if (this.debug) {
            this.plugin.getLogger().info("Top damage reset for mob: " + mobId);
        }
        this.saveYamlConfig();
    }

    public void resetDeathCountForMob(String mobId, String mobName) {
        this.yamlConfig.set("id." + mobId + ".mobs." + mobName + ".players.1.deaths", null);
        if (this.debug) {
            this.plugin.getLogger().info("Death count reset for mob: " + mobName);
        }
        this.saveYamlConfig();
    }
}
