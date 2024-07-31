package net.justforfun.MMTracker.commands;

import net.justforfun.MMTracker.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MMTrackerTabCompleter implements TabCompleter {

    private final Main plugin;

    public MMTrackerTabCompleter(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "reset");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            return getMobNamesFromConfig();
        }
        return new ArrayList<>();
    }

    private List<String> getMobNamesFromConfig() {
        List<String> mobNames = new ArrayList<>();
        List<?> mobs = plugin.getConfigManager().getConfig().getList("mythicmobs.specific_mobs");
        if (mobs != null) {
            for (Object mobConfig : mobs) {
                if (mobConfig instanceof Map) {
                    Map<String, Object> mobMap = (Map<String, Object>) mobConfig;
                    mobNames.add((String) mobMap.get("name"));
                }
            }
        }
        return mobNames;
    }
}
