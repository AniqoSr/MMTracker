package net.justforfun.MMTracker;

import net.justforfun.MMTracker.commands.MMTrackerCommand;
import net.justforfun.MMTracker.commands.MMTrackerTabCompleter;
import net.justforfun.MMTracker.configs.ConfigManager;
import net.justforfun.MMTracker.listeners.DamageListener;
import net.justforfun.MMTracker.placeholders.MMTrackerPlaceholder;
import net.justforfun.MMTracker.storage.Database;
import net.justforfun.MMTracker.tasks.SchedulerTask;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {
    private static Main instance;
    private Database database;
    private ConfigManager configManager;
    private FileConfiguration yamlConfig;
    private File yamlFile;

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("Enabling MMTracker...");

        // Save default config if it doesn't exist
        this.saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.setupYamlStorage();
        this.getLogger().info("YAML storage initialized: " + (this.database != null));
        this.getServer().getPluginManager().registerEvents(new DamageListener(this, this.database), this);
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            MMTrackerPlaceholder placeholder = new MMTrackerPlaceholder(this);
            placeholder.register();
            this.getLogger().info("PlaceholderAPI found and MMTrackerPlaceholder registered.");
        } else {
            this.getLogger().warning("PlaceholderAPI not found. Placeholders will not be parsed.");
        }
        this.getCommand("mmtracker").setExecutor(new MMTrackerCommand(this, this.database));
        this.getCommand("mmtracker").setTabCompleter(new MMTrackerTabCompleter(this));
        this.getLogger().info("MMTracker has been enabled.");

        // Initialize and schedule the SchedulerTask
        new SchedulerTask(this, this.database).runTaskTimer(this, 0L, 20L * 60); // runs every minute
    }

    @Override
    public void onDisable() {
        if (this.yamlConfig != null) {
            this.saveYamlConfig();
        }
        this.getLogger().info("MMTracker has been disabled.");
    }

    private void setupYamlStorage() {
        this.yamlFile = new File(this.getDataFolder(), ".data/topdamage.yml");
        if (!this.yamlFile.exists()) {
            this.yamlFile.getParentFile().mkdirs();
            try {
                this.yamlFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.yamlConfig = YamlConfiguration.loadConfiguration(this.yamlFile);
        this.database = new Database(this, this.configManager, this.yamlConfig, this.yamlFile);
        this.getLogger().info("YAML storage setup complete.");
    }

    private void saveYamlConfig() {
        try {
            this.yamlConfig.save(this.yamlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public Database getDatabase() {
        return this.database;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public void reloadConfigAndStorage() {
        this.configManager.reloadConfig();
        this.setupYamlStorage();
    }
}
