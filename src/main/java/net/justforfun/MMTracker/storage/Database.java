package net.justforfun.MMTracker.storage;

import net.justforfun.MMTracker.configs.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class Database {
    private final ConfigManager configManager;
    private final FileConfiguration yamlConfig;
    private final File yamlFile;

    public Database(ConfigManager configManager, FileConfiguration yamlConfig, File yamlFile) {
        this.configManager = configManager;
        this.yamlConfig = yamlConfig;
        this.yamlFile = yamlFile;
    }

    public String getStorageType() {
        return "yaml";
    }

    public FileConfiguration getYamlConfig() {
        return yamlConfig;
    }

    public int getDeathCount(String mobName) {
        return yamlConfig.getInt("deaths." + mobName, 0);
    }

    public void increaseDeathCount(String mobName) {
        int currentDeathCount = yamlConfig.getInt("deaths." + mobName, 0);
        yamlConfig.set("deaths." + mobName, currentDeathCount + 1);
        saveYamlConfig();
    }

    public void updateStorage(String mobName, String playerName, int damage) {
        String path = mobName + "." + playerName;
        int currentDamage = yamlConfig.getInt(path, 0);
        yamlConfig.set(path, currentDamage + damage);
        saveYamlConfig();
    }

    private void saveYamlConfig() {
        try {
            yamlConfig.save(yamlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
