package net.justforfun.MMTracker.listeners;

import net.justforfun.MMTracker.configs.ConfigManager;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.storage.Database;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;

public class DamageListener implements Listener {
    private final Database database;
    private final Main plugin;
    private final ConfigManager configManager;
    private final boolean debug;

    public DamageListener(Main plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
        this.configManager = plugin.getConfigManager();
        this.debug = configManager.isDebug();
        if (debug) {
            plugin.getLogger().info("DamageListener initialized with Database: " + (this.database != null));
        }
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

        if (debug) {
            plugin.getLogger().info("EntityDamageByEntityEvent: player=" + player.getName() + ", mobName=" + mobName + ", damage=" + damage);
        }

        int maxDeaths = getMaxDeaths(mobName);
        if (maxDeaths == -1) return; // Mob is not tracked

        int deathCount = database.getDeathCount(mobName);
        if (deathCount >= maxDeaths) return; // Stop recording damage

        // Update storage with damage
        database.updateStorage(mobName, player.getName(), (int) Math.round(damage));
        if (debug) {
            plugin.getLogger().info("Updated damage for player=" + player.getName() + ", mobName=" + mobName + ", damage=" + damage);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        // Check if the entity is a MythicMob
        ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        if (mob == null) return;

        String mobName = mob.getType().getInternalName();

        if (debug) {
            plugin.getLogger().info("EntityDeathEvent: mobName=" + mobName);
        }

        int maxDeaths = getMaxDeaths(mobName);
        if (maxDeaths == -1) return; // Mob is not tracked

        // Increase death count on entity death
        database.increaseDeathCount(mobName);
        if (debug) {
            plugin.getLogger().info("Increased death count for mobName=" + mobName);
        }
    }

    private int getMaxDeaths(String mobName) {
        for (Object mobConfig : configManager.getConfig().getList("mythicmobs.specific_mobs")) {
            if (mobConfig instanceof Map) {
                Map<String, Object> mobMap = (Map<String, Object>) mobConfig;
                if (mobName.equalsIgnoreCase((String) mobMap.get("name"))) {
                    return (int) mobMap.get("max_deaths");
                }
            }
        }
        return -1;
    }
}
