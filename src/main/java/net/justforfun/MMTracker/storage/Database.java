package net.justforfun.MMTracker.storage;

import net.justforfun.MMTracker.configs.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private final ConfigManager configManager;
    private FileConfiguration yamlConfig;
    private File yamlFile;
    private Connection connection;
    private String storageType;

    public Database(ConfigManager configManager) {
        this.configManager = configManager;
        this.storageType = configManager.getConfig().getString("storage.type");
        setupStorage();
    }

    private void setupStorage() {
        if (storageType.equalsIgnoreCase("yaml")) {
            setupYaml();
        } else {
            setupDatabase();
        }
    }

    private void setupYaml() {
        yamlFile = new File(configManager.getPlugin().getDataFolder(), ".data/topdamage.yml");
        if (!yamlFile.exists()) {
            yamlFile.getParentFile().mkdirs();
            try {
                yamlFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        yamlConfig = YamlConfiguration.loadConfiguration(yamlFile);
    }

    private void setupDatabase() {
        if (storageType.equalsIgnoreCase("sqlite")) {
            String dbPath = configManager.getPlugin().getDataFolder() + "/.data/topdamage.db";
            openConnection("jdbc:sqlite:" + dbPath, null, null);
        } else if (storageType.equalsIgnoreCase("mysql")) {
            String mysqlUrl = "jdbc:mysql://" + configManager.getConfig().getString("mysql.host") + ":" +
                    configManager.getConfig().getString("mysql.port") + "/" +
                    configManager.getConfig().getString("mysql.database");
            String mysqlUser = configManager.getConfig().getString("mysql.username");
            String mysqlPassword = configManager.getConfig().getString("mysql.password");
            openConnection(mysqlUrl, mysqlUser, mysqlPassword);
        }
        setupDatabaseTables();
    }

    private void openConnection(String url, String user, String password) {
        try {
            if (user == null || password == null) {
                connection = DriverManager.getConnection(url);
            } else {
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupDatabaseTables() {
        try (Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS top_damage (" +
                    "mob_name TEXT, " +
                    "player_name TEXT, " +
                    "damage_amount INTEGER, " +
                    "UNIQUE(mob_name, player_name))";
            stmt.execute(sql);

            String sqlDeathCount = "CREATE TABLE IF NOT EXISTS mob_deaths (" +
                    "mob_name TEXT, " +
                    "death_count INTEGER, " +
                    "PRIMARY KEY(mob_name))";
            stmt.execute(sqlDeathCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getStorageType() {
        return storageType;
    }

    public Connection getConnection() {
        if (connection == null || !isConnectionValid()) {
            setupDatabase(); // Reinitialize connection if it's not valid
        }
        return connection;
    }

    private boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public int getDeathCount(String mobName) {
        if (storageType.equalsIgnoreCase("yaml")) {
            return yamlConfig.getInt("deaths." + mobName, 0);
        } else {
            return getDeathCountFromDatabase(mobName);
        }
    }

    private int getDeathCountFromDatabase(String mobName) {
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT death_count FROM mob_deaths WHERE mob_name = ?")) {
            stmt.setString(1, mobName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("death_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void increaseDeathCount(String mobName) {
        if (storageType.equalsIgnoreCase("yaml")) {
            int currentDeathCount = yamlConfig.getInt("deaths." + mobName, 0);
            yamlConfig.set("deaths." + mobName, currentDeathCount + 1);
            try {
                yamlConfig.save(yamlFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            increaseDeathCountInDatabase(mobName);
        }
    }

    private void increaseDeathCountInDatabase(String mobName) {
        try (PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO mob_deaths (mob_name, death_count) VALUES (?, 1) ON DUPLICATE KEY UPDATE death_count = death_count + 1")) {
            stmt.setString(1, mobName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStorage(String mobName, String playerName, int damage) {
        if (storageType.equalsIgnoreCase("yaml")) {
            String path = mobName + "." + playerName;
            int currentDamage = yamlConfig.getInt(path, 0);
            yamlConfig.set(path, currentDamage + damage);
            try {
                yamlConfig.save(yamlFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            updateDatabaseStorage(mobName, playerName, damage);
        }
    }

    private void updateDatabaseStorage(String mobName, String playerName, int damage) {
        try (PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO top_damage (mob_name, player_name, damage_amount) VALUES (?, ?, ?) ON CONFLICT(mob_name, player_name) DO UPDATE SET damage_amount = damage_amount + ?")) {
            stmt.setString(1, mobName);
            stmt.setString(2, playerName);
            stmt.setInt(3, damage);
            stmt.setInt(4, damage);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> getTopDamage(String mobName) {
        Map<String, Integer> topDamage = new HashMap<>();
        if (storageType.equalsIgnoreCase("yaml")) {
            if (!yamlConfig.contains(mobName)) return topDamage;
            yamlConfig.getConfigurationSection(mobName).getKeys(false).forEach(player -> {
                int damage = yamlConfig.getInt(mobName + "." + player);
                topDamage.put(player, damage);
            });
        } else {
            try (PreparedStatement stmt = getConnection().prepareStatement("SELECT player_name, damage_amount FROM top_damage WHERE mob_name = ? ORDER BY damage_amount DESC")) {
                stmt.setString(1, mobName);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    topDamage.put(rs.getString("player_name"), rs.getInt("damage_amount"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return topDamage;
    }
}
