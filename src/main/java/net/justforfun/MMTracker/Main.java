package net.justforfun.MMTracker;

import net.justforfun.MMTracker.commands.MMTrackerCommand;
import net.justforfun.MMTracker.placeholders.TopDamagePlaceholders;
import net.justforfun.MMTracker.commands.MMTrackerTabCompleter;
import net.justforfun.MMTracker.storage.DatabaseHandler;
import net.justforfun.MMTracker.listeners.DamageListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {
    private DatabaseHandler databaseHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Ensure the default config file is saved if it doesn't exist
        setupStorage();

        getServer().getPluginManager().registerEvents(new DamageListener(this, databaseHandler), this);

        // Register the PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TopDamagePlaceholders(this).register();
        }

        getCommand("mmtracker").setExecutor(new MMTrackerCommand(this, databaseHandler));
        getCommand("mmtracker").setTabCompleter(new MMTrackerTabCompleter());
    }

    @Override
    public void onDisable() {
        if (databaseHandler != null) {
            databaseHandler.closeConnection();
        }
    }

    public void setupStorage() {
        if (databaseHandler != null) {
            databaseHandler.closeConnection();
        }

        String storageType = getConfig().getString("storage.type");
        if (storageType.equalsIgnoreCase("sqlite")) {
            databaseHandler = new DatabaseHandler("sqlite", getDataFolder() + "/.data/topdamage.db", null, null, null);
        } else if (storageType.equalsIgnoreCase("mysql")) {
            String mysqlUrl = "jdbc:mysql://" + getConfig().getString("mysql.host") + ":" +
                    getConfig().getString("mysql.port") + "/" +
                    getConfig().getString("mysql.database");
            String mysqlUser = getConfig().getString("mysql.username");
            String mysqlPassword = getConfig().getString("mysql.password");
            databaseHandler = new DatabaseHandler("mysql", null, mysqlUrl, mysqlUser, mysqlPassword);
        }
        if (databaseHandler != null) {
            databaseHandler.setupDatabase();
        }
    }

    public DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    public void reloadConfigAndStorage() {
        reloadConfig();
        setupStorage();
    }
}