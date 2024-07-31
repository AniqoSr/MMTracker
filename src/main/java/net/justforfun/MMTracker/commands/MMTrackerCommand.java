package net.justforfun.MMTracker.commands;

import net.justforfun.MMTracker.storage.Database;
import net.justforfun.MMTracker.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MMTrackerCommand implements CommandExecutor {
    private final Main plugin;
    private final Database database;

    public MMTrackerCommand(Main plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/mmtracker <reload|reset>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfigAndStorage(); // Reload config and storage
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
        FileConfiguration yamlConfig = database.getYamlConfig();
        yamlConfig.set(mobName, null);
        try {
            yamlConfig.save(new File(plugin.getDataFolder(), ".data/topdamage.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetDeathCount(String mobName) {
        FileConfiguration yamlConfig = database.getYamlConfig();
        yamlConfig.set("deaths." + mobName, 0);
        try {
            yamlConfig.save(new File(plugin.getDataFolder(), ".data/topdamage.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
