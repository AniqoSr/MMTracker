package net.justforfun.MMTracker.listeners;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.justforfun.MMTracker.Main;
import net.justforfun.MMTracker.storage.Database;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class DamageListener implements Listener {
    private final Database database;
    private final Main plugin;
    private final boolean debug;

    public DamageListener(Main plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
        this.debug = plugin.getConfigManager().isDebug();
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
        double damage = event.getDamage();
        if (this.debug) {
            this.plugin.getLogger().info("EntityDamageByEntityEvent: player=" + player.getName() + ", mobName=" + mobName + ", damage=" + damage);
        }
        int deathCount = this.database.getDeathCount(mobName);
        int maxDeaths = this.plugin.getConfigManager().getMaxDeaths(mobName);
        if (maxDeaths != -1 && deathCount >= maxDeaths) {
            if (this.debug) {
                this.plugin.getLogger().info("Max deaths reached for " + mobName + ". No more updates.");
            }
            return;
        }
        this.database.updateStorage(mobName, player.getName(), (int) Math.round(damage));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        if (mob == null) {
            return;
        }
        String mobName = mob.getType().getInternalName();
        if (this.debug) {
            this.plugin.getLogger().info("EntityDeathEvent: mobName=" + mobName);
        }
        this.database.increaseDeathCount(mobName);
    }
}
