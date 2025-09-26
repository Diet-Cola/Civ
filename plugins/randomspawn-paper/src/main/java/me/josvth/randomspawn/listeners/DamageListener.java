package me.josvth.randomspawn.listeners;

import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.RandomSpawnUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 * Prevent players from taking damage if they are within the invulnerability period.
 */
public class DamageListener implements Listener {

    RandomSpawn plugin;

    public DamageListener(RandomSpawn instance) {
        plugin = instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && RandomSpawnUtils.hasLastTimeRandomSpawned(player) && !event.getCause().equals(DamageCause.SUICIDE)) {
            if ((RandomSpawnUtils.getLastTimeRandomSpawned(player) + (plugin.configs.config.damageImmunityPeriod() * 1000)) > System.currentTimeMillis()) {
                event.setCancelled(true);
            }
        }
    }
}
