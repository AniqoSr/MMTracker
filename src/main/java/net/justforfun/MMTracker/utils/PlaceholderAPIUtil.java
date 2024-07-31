package net.justforfun.MMTracker.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.storage.DatabaseHandler;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaceholderAPIUtil {

    private Main plugin;
    private DatabaseHandler databaseHandler;
    private FileConfiguration yamlConfig;

    public PlaceholderAPIUtil(Main plugin) {
        this.plugin = plugin;
        this.databaseHandler = plugin.getDatabaseHandler();

        // Load YAML configuration if using YAML storage
        if (plugin.getConfig().getString("storage.type").equalsIgnoreCase("yaml")) {
            File yamlFile = new File(plugin.getDataFolder(), ".data/topdamage.yml");
            this.yamlConfig = YamlConfiguration.loadConfiguration(yamlFile);
        }
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
            return getTopName(mobName, rank);
        } else if ("topdamage".equalsIgnoreCase(type)) {
            return String.valueOf(getTopDamage(mobName, rank));
        }

        return "None";
    }

    private String getTopName(String mobName, int rank) {
        String storageType = plugin.getConfig().getString("storage.type");
        if (storageType.equalsIgnoreCase("yaml")) {
            return getTopNameFromYaml(mobName, rank);
        } else {
            return getTopNameFromDatabase(mobName, rank);
        }
    }

    private int getTopDamage(String mobName, int rank) {
        String storageType = plugin.getConfig().getString("storage.type");
        if (storageType.equalsIgnoreCase("yaml")) {
            return getTopDamageFromYaml(mobName, rank);
        } else {
            return getTopDamageFromDatabase(mobName, rank);
        }
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

    private String getTopNameFromDatabase(String mobName, int rank) {
        try (Connection conn = databaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT player_name FROM top_damage WHERE mob_name = ? ORDER BY damage_amount DESC LIMIT 1 OFFSET ?")) {
            stmt.setString(1, mobName);
            stmt.setInt(2, rank - 1);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("player_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "None";
    }

    private int getTopDamageFromDatabase(String mobName, int rank) {
        try (Connection conn = databaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT damage_amount FROM top_damage WHERE mob_name = ? ORDER BY damage_amount DESC LIMIT 1 OFFSET ?")) {
            stmt.setString(1, mobName);
            stmt.setInt(2, rank - 1);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("damage_amount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}