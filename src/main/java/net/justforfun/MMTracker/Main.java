package net.justforfun.MMTracker;

import net.justforfun.MMTracker.commands.MMTrackerCommand;
import net.justforfun.MMTracker.configs.ConfigManager;
import net.justforfun.MMTracker.placeholders.TopDamagePlaceholders;
import net.justforfun.MMTracker.commands.MMTrackerTabCompleter;
import net.justforfun.MMTracker.listeners.DamageListener;
import net.justforfun.MMTracker.storage.Database;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Main extends JavaPlugin {
    private Database database;
    private ConfigManager configManager;
    private FileConfiguration yamlConfig;
    private File yamlFile;

    @Override
    public void onEnable() {
        getLogger().info("Enabling MMTracker...");
        configManager = new ConfigManager(this); // Initialize ConfigManager
        setupYamlStorage();
        getLogger().info("YAML storage initialized: " + (database != null));

        getServer().getPluginManager().registerEvents(new DamageListener(this, database), this);

        // Register the PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TopDamagePlaceholders(this).register();
        }

        getCommand("mmtracker").setExecutor(new MMTrackerCommand(this, database));
        getCommand("mmtracker").setTabCompleter(new MMTrackerTabCompleter(this));
    }

    @Override
    public void onDisable() {
        saveYamlConfig();
    }

    private void setupYamlStorage() {
        yamlFile = new File(getDataFolder(), ".data/topdamage.yml");
        if (!yamlFile.exists()) {
            yamlFile.getParentFile().mkdirs();
            try {
                yamlFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        yamlConfig = YamlConfiguration.loadConfiguration(yamlFile);
        database = new Database(configManager, yamlConfig, yamlFile);
        getLogger().info("YAML storage setup complete.");
    }

    public Database getDatabase() {
        return database;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void reloadConfigAndStorage() {
        // Reload the configuration from the ConfigManager
        configManager.reloadConfig();

        // Reload YAML storage
        setupYamlStorage();
    }

    private void saveYamlConfig() {
        try {
            yamlConfig.save(yamlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
