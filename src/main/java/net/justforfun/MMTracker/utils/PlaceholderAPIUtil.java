package net.justforfun.MMTracker.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import net.justforfun.MMTracker.Main;

public class PlaceholderAPIUtil {

    private final Main plugin;
    private final FileConfiguration yamlConfig;

    public PlaceholderAPIUtil(Main plugin) {
        this.plugin = plugin;
        this.yamlConfig = plugin.getDatabase().getYamlConfig();
    }

    public String parsePlaceholder(Player player, String identifier) {
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

        if ("topname".equalsIgnoreCase(type)) {
            return getTopNameFromYaml(mobName, rank);
        } else if ("topdamage".equalsIgnoreCase(type)) {
            return String.valueOf(getTopDamageFromYaml(mobName, rank));
        }

        return "None";
    }

    private String getTopNameFromYaml(String mobName, int rank) {
        if (!yamlConfig.contains(mobName)) return "None";
        return yamlConfig.getConfigurationSection(mobName).getKeys(false).stream()
                .sorted((p1, p2) -> Integer.compare(yamlConfig.getInt(mobName + "." + p2), yamlConfig.getInt(mobName + "." + p1)))
                .skip(rank - 1)
                .findFirst()
                .orElse("None");
    }

    private int getTopDamageFromYaml(String mobName, int rank) {
        if (!yamlConfig.contains(mobName)) return 0;
        return yamlConfig.getConfigurationSection(mobName).getKeys(false).stream()
                .sorted((p1, p2) -> Integer.compare(yamlConfig.getInt(mobName + "." + p2), yamlConfig.getInt(mobName + "." + p1)))
                .skip(rank - 1)
                .findFirst()
                .map(p -> yamlConfig.getInt(mobName + "." + p))
                .orElse(0);
    }
}
