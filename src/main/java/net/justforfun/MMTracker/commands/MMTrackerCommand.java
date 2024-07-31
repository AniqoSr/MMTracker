package net.justforfun.MMTracker.commands;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.justforfun.MMTracker.storage.DatabaseHandler;
import net.justforfun.MMTracker.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MMTrackerCommand implements CommandExecutor {
    private Main plugin;
    private DatabaseHandler databaseHandler;

    public MMTrackerCommand(Main plugin, DatabaseHandler databaseHandler) {
        this.plugin = plugin;
        this.databaseHandler = databaseHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/mmtracker <reload|reset>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                plugin.setupStorage(); // Reinitialize storage based on new config
                sender.sendMessage("Config and storage reloaded.");
                break;
            case "reset":
                if (args.length < 2) {
                    sender.sendMessage("Please specify the mob name.");
                    return true;
                }
                resetTopDamage(args[1]);
                resetDeathCount(args[1]);
                sender.sendMessage("Top damage and death count for " + args[1] + " reset.");
                break;
            default:
                sender.sendMessage("/mmtracker <reload|reset>");
                break;
        }
        return true;
    }

    private void resetTopDamage(String mobName) {
        String storageType = plugin.getConfig().getString("storage.type");
        if (storageType.equalsIgnoreCase("yaml")) {
            resetTopDamageYaml(mobName);
        } else {
            resetTopDamageDatabase(mobName);
        }
    }

    private void resetTopDamageYaml(String mobName) {
        FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), ".data/topdamage.yml"));
        yamlConfig.set(mobName, null);
        try {
            yamlConfig.save(new File(plugin.getDataFolder(), ".data/topdamage.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetTopDamageDatabase(String mobName) {
        databaseHandler.openConnection();
        try (Connection conn = databaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM top_damage WHERE mob_name = ?")) {
            stmt.setString(1, mobName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            databaseHandler.closeConnection();
        }
    }

    private void resetDeathCount(String mobName) {
        String storageType = plugin.getConfig().getString("storage.type");
        if (storageType.equalsIgnoreCase("yaml")) {
            resetDeathCountYaml(mobName);
        } else {
            resetDeathCountDatabase(mobName);
        }
    }

    private void resetDeathCountYaml(String mobName) {
        FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), ".data/topdamage.yml"));
        yamlConfig.set("deaths." + mobName, 0);
        try {
            yamlConfig.save(new File(plugin.getDataFolder(), ".data/topdamage.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetDeathCountDatabase(String mobName) {
        databaseHandler.openConnection();
        try (Connection conn = databaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM mob_deaths WHERE mob_name = ?")) {
            stmt.setString(1, mobName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            databaseHandler.closeConnection();
        }
    }
}
