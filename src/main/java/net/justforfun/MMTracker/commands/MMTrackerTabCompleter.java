package net.justforfun.MMTracker.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.justforfun.MMTracker.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;

public class MMTrackerTabCompleter implements TabCompleter {
    private final Main plugin;

    public MMTrackerTabCompleter(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "reset");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            return this.getMobIdsFromConfig();
        }
        return new ArrayList<>();
    }

    private List<String> getMobIdsFromConfig() {
        List<String> mobIds = new ArrayList<>();
        ConfigurationSection section = this.plugin.getConfigManager().getConfig().getConfigurationSection("mythicmobs.spesifik_mob");
        if (section != null) {
            mobIds.addAll(section.getKeys(false));
        }
        return mobIds;
    }
}
