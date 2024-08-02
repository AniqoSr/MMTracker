package net.justforfun.MMTracker.commands;

import java.io.File;
import java.io.IOException;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.storage.Database;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class MMTrackerCommand implements CommandExecutor {
    private final Main plugin;
    private final Database database;

    public MMTrackerCommand(Main plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("mmtracker.use")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("/mmtracker <reload|reset>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload": {
                this.plugin.reloadConfigAndStorage();
                sender.sendMessage("Config and storage reloaded.");
                break;
            }
            case "reset": {
                if (args.length < 2) {
                    sender.sendMessage("Please specify the id.");
                    return true;
                }
                this.resetTopDamage(args[1]);
                sender.sendMessage("Top damage and death count for " + args[1] + " reset.");
                break;
            }
            default: {
                sender.sendMessage("/mmtracker <reload|reset>");
            }
        }
        return true;
    }

    private void resetTopDamage(String id) {
        this.database.resetTopDamageForMob(id);
        for (String mobName : this.plugin.getConfigManager().getMobNames(id)) {
            this.database.resetDeathCountForMob(id, mobName);
        }
    }
}
