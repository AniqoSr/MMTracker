package net.justforfun.MMTracker.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import net.justforfun.MMTracker.Main;

public class PlaceholderAPIUtil {

    private static PlaceholderAPIUtil instance;
    private final Main plugin;
    private final FileConfiguration yamlConfig;
    private final boolean debug;

    public PlaceholderAPIUtil(Main plugin) {
        this.plugin = plugin;
        this.yamlConfig = plugin.getDatabase().getYamlConfig();
        this.debug = plugin.getConfigManager().isDebug();
        instance = this;
    }

    public static PlaceholderAPIUtil getInstance() {
        return instance;
    }

    public String getPlaceholderValue(Player player, String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return "None";
        }

        String[] parts = identifier.split("_");
        if (parts.length != 4) {
            return "None";
        }

        String type = parts[1];
        String mobName = parts[2];
        int rank;

        try {
            rank = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return "None";
        }

        if (debug) {
            plugin.getLogger().info("Parsing placeholder: type=" + type + ", mobName=" + mobName + ", rank=" + rank);
        }

        if ("topname".equalsIgnoreCase(type)) {
            String topName = getTopNameFromYaml(mobName, rank);
            if (debug) {
                plugin.getLogger().info("Parsed top name: " + topName);
            }
            return topName;
        } else if ("topdamage".equalsIgnoreCase(type)) {
            int topDamage = getTopDamageFromYaml(mobName, rank);
            if (debug) {
                plugin.getLogger().info("Parsed top damage: " + topDamage);
            }
            return String.valueOf(topDamage);
        }

        return "None";
    }

    private String getTopNameFromYaml(String mobName, int rank) {
        String key = rank + ".Name";
        String name = yamlConfig.getString(mobName + "." + key, "None");
        if (debug) {
            plugin.getLogger().info("Fetching top name for " + mobName + " rank " + rank + ": " + name);
        }
        return name;
    }

    private int getTopDamageFromYaml(String mobName, int rank) {
        String key = rank + ".Damage";
        int damage = yamlConfig.getInt(mobName + "." + key, 0);
        if (debug) {
            plugin.getLogger().info("Fetching top damage for " + mobName + " rank " + rank + ": " + damage);
        }
        return damage;
    }
}
