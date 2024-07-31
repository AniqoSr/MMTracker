package net.justforfun.MMTracker;

import net.justforfun.MMTracker.commands.MMTrackerCommand;
import net.justforfun.MMTracker.placeholders.TopDamagePlaceholders;
import net.justforfun.MMTracker.commands.MMTrackerTabCompleter;
import net.justforfun.MMTracker.listeners.DamageListener;
import net.justforfun.MMTracker.configs.ConfigManager;
import net.justforfun.MMTracker.storage.Database;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private Database database;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        getLogger().info("Enabling MMTracker...");
        configManager = new ConfigManager(this); // Initialize ConfigManager
        database = new Database(configManager); // Initialize Database
        getLogger().info("Database initialized: " + (database != null));

        getServer().getPluginManager().registerEvents(new DamageListener(this, database), this);

        // Register the PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TopDamagePlaceholders(this).register();
        }

        getCommand("mmtracker").setExecutor(new MMTrackerCommand(this, database));
        getCommand("mmtracker").setTabCompleter(new MMTrackerTabCompleter());
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.closeConnection();
        }
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

        // Setup storage with the reloaded configuration
        database = new Database(configManager);
    }
}
