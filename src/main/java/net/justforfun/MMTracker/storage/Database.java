package net.justforfun.MMTracker.storage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.configs.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;
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

    public int getDeathCount(String mobId, String mobName) {
        return this.yamlConfig.getInt("id." + mobId + ".mobs." + mobName + ".deaths", 0);
    }

    public void increaseDeathCount(String mobId, String mobName) {
        int currentDeathCount = this.yamlConfig.getInt("id." + mobId + ".mobs." + mobName + ".deaths", 0);
        this.yamlConfig.set("id." + mobId + ".mobs." + mobName + ".deaths", currentDeathCount + 1);
        if (this.debug) {
            this.plugin.getLogger().info("Death count increased for " + mobName + " in " + mobId + " to " + (currentDeathCount + 1));
        }
        this.saveYamlConfig();
    }

    public void updateStorage(String mobId, String mobName, String playerName, int damage) {
        Map<String, Integer> playerDamageMap = new HashMap<>();
        String basePath = "id." + mobId + ".mobs." + mobName;

        if (this.yamlConfig.contains(basePath + ".players")) {
            for (String key : this.yamlConfig.getConfigurationSection(basePath + ".players").getKeys(false)) {
                String name = this.yamlConfig.getString(basePath + ".players." + key + ".Name");
                int dmg = this.yamlConfig.getInt(basePath + ".players." + key + ".Damage");
                playerDamageMap.put(name, dmg);
            }
        }

        playerDamageMap.put(playerName, playerDamageMap.getOrDefault(playerName, 0) + damage);

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortByValue(playerDamageMap).entrySet()) {
            this.yamlConfig.set(basePath + ".players." + rank + ".Name", entry.getKey());
            this.yamlConfig.set(basePath + ".players." + rank + ".Damage", entry.getValue());
            rank++;
        }

        updateTotalDamage(mobId, playerName, damage);

        if (this.debug) {
            this.plugin.getLogger().info("Updated damage for player=" + playerName + ", mobName=" + mobName + ", damage=" + damage);
        }

        this.saveYamlConfig();
    }

    private void updateTotalDamage(String mobId, String playerName, int damage) {
        Map<String, Integer> playerTotalDamageMap = new HashMap<>();
        String totalDamagePath = "id." + mobId + ".totaldamage.players";

        if (this.yamlConfig.contains(totalDamagePath)) {
            for (String key : this.yamlConfig.getConfigurationSection(totalDamagePath).getKeys(false)) {
                String name = this.yamlConfig.getString(totalDamagePath + "." + key + ".Name");
                int dmg = this.yamlConfig.getInt(totalDamagePath + "." + key + ".Damage");
                playerTotalDamageMap.put(name, dmg);
            }
        }

        playerTotalDamageMap.put(playerName, playerTotalDamageMap.getOrDefault(playerName, 0) + damage);

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortByValue(playerTotalDamageMap).entrySet()) {
            this.yamlConfig.set(totalDamagePath + "." + rank + ".Name", entry.getKey());
            this.yamlConfig.set(totalDamagePath + "." + rank + ".Damage", entry.getValue());
            rank++;
        }
    }

    public void resetTopDamageForMob(String mobId) {
        this.yamlConfig.set("id." + mobId, null);
        if (this.debug) {
            this.plugin.getLogger().info("Top damage reset for mob: " + mobId);
        }
        this.saveYamlConfig();
    }

    public void resetDeathCountForMob(String mobId) {
        ConfigurationSection mobSection = this.yamlConfig.getConfigurationSection("id." + mobId + ".mobs");
        if (mobSection != null) {
            for (String mobName : mobSection.getKeys(false)) {
                this.yamlConfig.set("id." + mobId + ".mobs." + mobName + ".deaths", null);
            }
        }
        if (this.debug) {
            this.plugin.getLogger().info("Death count reset for all mobs in: " + mobId);
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
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
