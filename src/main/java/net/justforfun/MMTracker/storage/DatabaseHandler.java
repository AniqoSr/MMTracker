package net.justforfun.MMTracker.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {
    private Connection connection;
    private String dbType;
    private String dbPath;
    private String mysqlUrl;
    private String mysqlUser;
    private String mysqlPassword;

    public DatabaseHandler(String dbType, String dbPath, String mysqlUrl, String mysqlUser, String mysqlPassword) {
        this.dbType = dbType;
        this.dbPath = dbPath;
        this.mysqlUrl = mysqlUrl;
        this.mysqlUser = mysqlUser;
        this.mysqlPassword = mysqlPassword;
        openConnection();
    }

    public void openConnection() {
        try {
            if (dbType.equalsIgnoreCase("sqlite")) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            } else {
                connection = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
            }
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

    public void setupDatabase() {
        openConnection();
        try (Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS top_damage (" +
                    "mob_name TEXT, " +
                    "player_name TEXT, " +
                    "damage_amount INTEGER, " +
                    "UNIQUE(mob_name, player_name))"; // Add UNIQUE constraint
            stmt.execute(sql);

            String sqlDeathCount = "CREATE TABLE IF NOT EXISTS mob_deaths (" +
                    "mob_name TEXT, " +
                    "death_count INTEGER, " +
                    "PRIMARY KEY(mob_name))";
            stmt.execute(sqlDeathCount);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public Connection getConnection() {
        openConnection(); // Ensure connection is open before returning
        return connection;
    }

    public String getStorageType() {
        return dbType;
    }
}