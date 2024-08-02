package net.justforfun.MMTracker.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.configs.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

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
        return this.yamlConfig;
    }

    public int getDeathCount(String mobName) {
        return this.yamlConfig.getInt("id." + mobName + ".deaths", 0);
    }

    public void increaseDeathCount(String mobName) {
        int currentDeathCount = this.yamlConfig.getInt("id." + mobName + ".deaths", 0);
        int maxDeaths = this.configManager.getMaxDeaths(mobName);
        if (maxDeaths != -1 && currentDeathCount >= maxDeaths) {
            if (this.debug) {
                this.plugin.getLogger().info("Max deaths reached for " + mobName + ". No more updates.");
            }
            return;
        }
        this.yamlConfig.set("id." + mobName + ".deaths", currentDeathCount + 1);
        if (this.debug) {
            this.plugin.getLogger().info("Death count increased for " + mobName + " to " + (currentDeathCount + 1));
        }
        this.saveYamlConfig();
    }

    public void updateStorage(String mobName, String playerName, int damage) {
        int currentDeathCount = this.yamlConfig.getInt("id." + mobName + ".deaths", 0);
        int maxDeaths = this.configManager.getMaxDeaths(mobName);
        if (maxDeaths != -1 && currentDeathCount >= maxDeaths) {
            if (this.debug) {
                this.plugin.getLogger().info("Max deaths reached for " + mobName + ". No more updates.");
            }
            return;
        }

        // Update individual mob damage
        String path = "id." + mobName + ".mobs." + mobName + ".players";
        HashMap<String, Integer> playerDamageMap = new HashMap<>();
        if (this.yamlConfig.contains(path)) {
            for (String key : this.yamlConfig.getConfigurationSection(path).getKeys(false)) {
                String name = this.yamlConfig.getString(path + "." + key + ".Name");
                int dmg = this.yamlConfig.getInt(path + "." + key + ".Damage");
                playerDamageMap.put(name, dmg);
            }
        }
        playerDamageMap.put(playerName, playerDamageMap.getOrDefault(playerName, 0) + damage);
        int rank = 1;
        for (Map.Entry<String, Integer> entry : Database.sortByValue(playerDamageMap).entrySet()) {
            this.yamlConfig.set(path + "." + rank + ".Name", entry.getKey());
            this.yamlConfig.set(path + "." + rank + ".Damage", entry.getValue());
            rank++;
        }

        // Update total damage
        path = "id." + mobName + ".totaldamage.players";
        playerDamageMap.clear();
        if (this.yamlConfig.contains(path)) {
            for (String key : this.yamlConfig.getConfigurationSection(path).getKeys(false)) {
                String name = this.yamlConfig.getString(path + "." + key + ".Name");
                int dmg = this.yamlConfig.getInt(path + "." + key + ".Damage");
                playerDamageMap.put(name, dmg);
            }
        }
        playerDamageMap.put(playerName, playerDamageMap.getOrDefault(playerName, 0) + damage);
        rank = 1;
        for (Map.Entry<String, Integer> entry : Database.sortByValue(playerDamageMap).entrySet()) {
            this.yamlConfig.set(path + "." + rank + ".Name", entry.getKey());
            this.yamlConfig.set(path + "." + rank + ".Damage", entry.getValue());
            rank++;
        }

        if (this.debug) {
            this.plugin.getLogger().info("Updated damage for player=" + playerName + ", mobName=" + mobName + ", damage=" + damage);
        }
        this.saveYamlConfig();
    }

    public void resetTopDamageForMob(String mobName) {
        this.yamlConfig.set("id." + mobName + ".mobs." + mobName + ".players", null);
        this.yamlConfig.set("id." + mobName + ".totaldamage.players", null);
        if (this.debug) {
            this.plugin.getLogger().info("Top damage reset for mob: " + mobName);
        }
        this.saveYamlConfig();
    }

    public void resetDeathCountForMob(String mobName) {
        this.yamlConfig.set("id." + mobName + ".deaths", null);
        if (this.debug) {
            this.plugin.getLogger().info("Death count reset for mob: " + mobName);
        }
        this.saveYamlConfig();
    }

    private void saveYamlConfig() {
        try {
            this.yamlConfig.save(this.yamlFile);
            if (this.debug) {
                this.plugin.getLogger().info("YAML configuration saved.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        ArrayList<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
