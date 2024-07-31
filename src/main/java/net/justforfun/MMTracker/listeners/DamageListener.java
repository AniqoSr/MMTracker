package net.justforfun.MMTracker.listeners;

import net.justforfun.MMTracker.storage.DatabaseHandler;
import net.justforfun.MMTracker.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DamageListener implements Listener {
    private DatabaseHandler databaseHandler;
    private Main plugin;
    private FileConfiguration yamlConfig;
    private File yamlFile;

    public DamageListener(Main plugin, DatabaseHandler databaseHandler) {
        this.plugin = plugin;
        this.databaseHandler = databaseHandler;
        setupYaml();
    }

    private void setupYaml() {
        yamlFile = new File(plugin.getDataFolder(), ".data/topdamage.yml");
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

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        Entity entity = event.getEntity();

        // Check if the entity is a MythicMob
        ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        if (mob == null) return;

        String mobName = mob.getType().getInternalName();
        double damage = event.getDamage();

        int maxDeaths = getMaxDeaths(mobName);
        if (maxDeaths == -1) return; // Mob is not tracked

        int deathCount = getDeathCount(mobName);
        if (deathCount >= maxDeaths) return; // Stop recording damage

        String storageType = databaseHandler.getStorageType();
        if (storageType.equalsIgnoreCase("yaml")) {
            updateYamlStorage(mobName, player.getName(), (int) Math.round(damage));
        } else {
            updateDatabaseStorage(mobName, player.getName(), (int) Math.round(damage));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        // Check if the entity is a MythicMob
        ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        if (mob == null) return;

        String mobName = mob.getType().getInternalName();

        int maxDeaths = getMaxDeaths(mobName);
        if (maxDeaths == -1) return; // Mob is not tracked

        increaseDeathCount(mobName);
    }

    private int getMaxDeaths(String mobName) {
        for (Object mobConfig : plugin.getConfig().getList("mythicmobs.specific_mobs")) {
            if (mobConfig instanceof Map) {
                Map<String, Object> mobMap = (Map<String, Object>) mobConfig;
                if (mobName.equalsIgnoreCase((String) mobMap.get("name"))) {
                    return (int) mobMap.get("max_deaths");
                }
            }
        }
        return -1;
    }

    private int getDeathCount(String mobName) {
        String storageType = databaseHandler.getStorageType();
        if (storageType.equalsIgnoreCase("yaml")) {
            return yamlConfig.getInt("deaths." + mobName, 0);
        } else {
            return getDeathCountFromDatabase(mobName);
        }
    }

    private int getDeathCountFromDatabase(String mobName) {
        try (Connection conn = databaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT death_count FROM mob_deaths WHERE mob_name = ?")) {
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

    private void increaseDeathCount(String mobName) {
        String storageType = databaseHandler.getStorageType();
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
        try (Connection conn = databaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO mob_deaths (mob_name, death_count) VALUES (?, 1) ON DUPLICATE KEY UPDATE death_count = death_count + 1")) {
            stmt.setString(1, mobName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateYamlStorage(String mobName, String playerName, int damage) {
        String path = mobName + "." + playerName;
        int currentDamage = yamlConfig.getInt(path, 0);
        yamlConfig.set(path, currentDamage + damage);
        try {
            yamlConfig.save(yamlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDatabaseStorage(String mobName, String playerName, int damage) {
        try (Connection conn = databaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO top_damage (mob_name, player_name, damage_amount) VALUES (?, ?, ?) ON CONFLICT(mob_name, player_name) DO UPDATE SET damage_amount = damage_amount + ?")) {
            stmt.setString(1, mobName);
            stmt.setString(2, playerName);
            stmt.setInt(3, damage);
            stmt.setInt(4, damage);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}