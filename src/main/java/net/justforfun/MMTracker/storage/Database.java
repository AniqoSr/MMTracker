package net.justforfun.MMTracker.storage;

import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.configs.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
        if (debug) {
            plugin.getLogger().info("Death count increased for " + mobName + " to " + (currentDeathCount + 1));
        }
        saveYamlConfig();
    }

    public void updateStorage(String mobName, String playerName, int damage) {
        // Get current damage for the player
        Map<String, Integer> playerDamageMap = new HashMap<>();
        if (yamlConfig.contains(mobName)) {
            for (String key : yamlConfig.getConfigurationSection(mobName).getKeys(false)) {
                String name = yamlConfig.getString(mobName + "." + key + ".Name");
                int dmg = yamlConfig.getInt(mobName + "." + key + ".Damage");
                playerDamageMap.put(name, playerDamageMap.getOrDefault(name, 0) + dmg);
            }
        }
        // Update damage
        playerDamageMap.put(playerName, playerDamageMap.getOrDefault(playerName, 0) + damage);

        // Save back to yamlConfig
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortByValue(playerDamageMap).entrySet()) {
            yamlConfig.set(mobName + "." + rank + ".Name", entry.getKey());
            yamlConfig.set(mobName + "." + rank + ".Damage", entry.getValue());
            rank++;
        }

        if (debug) {
            plugin.getLogger().info("Updated damage for player=" + playerName + ", mobName=" + mobName + ", damage=" + damage);
        }
        saveYamlConfig();
    }

    private void saveYamlConfig() {
        try {
            yamlConfig.save(yamlFile);
            if (debug) {
                plugin.getLogger().info("YAML configuration saved.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
