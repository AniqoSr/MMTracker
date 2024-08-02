package net.justforfun.MMTracker.listeners;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.configs.ConfigManager;
import net.justforfun.MMTracker.storage.Database;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;
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
        this.debug = this.configManager.isDebug();
        if (this.debug) {
            plugin.getLogger().info("DamageListener initialized with Database: " + (this.database != null));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        Entity entity = event.getEntity();
        ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        if (mob == null) {
            return;
        }
        String mobName = mob.getType().getInternalName();
        String mobId = getMobId(mobName);
        if (mobId == null) {
            return;
        }
        int maxDeaths = this.configManager.getMaxDeaths(mobId, mobName);
        double damage = event.getDamage();
        if (this.debug) {
            this.plugin.getLogger().info("EntityDamageByEntityEvent: player=" + player.getName() + ", mobName=" + mobName + ", damage=" + damage);
        }

        // Store the damage regardless of the maxDeaths value
        this.database.updateStorage(mobId, mobName, player.getName(), (int) Math.round(damage));
        if (this.debug) {
            this.plugin.getLogger().info("Updated damage for player=" + player.getName() + ", mobName=" + mobName + ", damage=" + damage);
        }

        // Check maxDeaths for death handling
        if (maxDeaths != -1 && this.database.getDeathCount(mobId, mobName) >= maxDeaths) {
            return;
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        if (mob == null) {
            return;
        }
        String mobName = mob.getType().getInternalName();
        String mobId = getMobId(mobName);
        if (mobId == null) {
            return;
        }
        if (this.debug) {
            this.plugin.getLogger().info("EntityDeathEvent: mobName=" + mobName);
        }
        int maxDeaths = this.configManager.getMaxDeaths(mobId, mobName);
        if (maxDeaths == -1 || this.database.getDeathCount(mobId, mobName) >= maxDeaths) {
            return;
        }
        this.database.increaseDeathCount(mobId, mobName);
        if (this.debug) {
            this.plugin.getLogger().info("Increased death count for mobName=" + mobName);
        }
    }

    private String getMobId(String mobName) {
        for (String id : this.configManager.getConfig().getConfigurationSection("mythicmobs.spesifik_mob").getKeys(false)) {
            Map<String, Object> mobData = this.configManager.getConfig().getConfigurationSection("mythicmobs.spesifik_mob." + id).getValues(false);
            if (mobData.containsKey("nama_mobs")) {
                List<String> namaMobs = (List<String>) mobData.get("nama_mobs");
                if (namaMobs.contains(mobName)) {
                    return id;
                }
            } else if (mobData.containsKey("nama_mob")) {
                List<String> namaMob = (List<String>) mobData.get("nama_mob");
                if (namaMob.contains(mobName)) {
                    return id;
                }
            }
        }
        return null;
    }
}
