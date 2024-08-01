package net.justforfun.MMTracker.configs;

import net.justforfun.MMTracker.Main;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final Main plugin;
    private FileConfiguration config;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
}
