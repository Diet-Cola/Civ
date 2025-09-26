package me.josvth.randomspawn.listeners;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.RandomSpawnUtils;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeListener implements Listener {

    RandomSpawn plugin;

    public WorldChangeListener(RandomSpawn instance) {
        plugin = instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {

        final Player player = event.getPlayer();
        String playerName = player.getName();

        if (player.hasPermission("RandomSpawn.exclude")) {                                 // checks if player should be excluded
            plugin.logDebug(playerName + " is excluded from Random Spawning.");
            return;
        }

        World from = event.getFrom();
        World to = player.getWorld();

        WorldConfig toConfig = plugin.configs.worlds.get(to.getName());
        if (toConfig == null) {
            plugin.logDebug("Could not find WorldConfig for " + to.getName());
        }

        final Location bedLocation = player.getRespawnLocation();
        if (bedLocation != null && to.equals(bedLocation.getWorld()))
            return; // players bed is in this world

        if (toConfig.randomSpawnOn().teleportsFrom().contains(from.getName())) {

            Location spawnLocation = plugin.getSpawnSelector().getRandomSpawn(to);

            if (spawnLocation == null) {
                plugin.logDebug(playerName + " got unlucky and was not successfully randomspawned. Default behavior will apply");
                return;
            }

            player.teleportAsync(spawnLocation);

            RandomSpawnUtils.setLastTimeRandomSpawned(plugin, player, System.currentTimeMillis());

            if (toConfig.keepRandomSpawn()) {
                player.setBedSpawnLocation(spawnLocation);
            }

            if (plugin.configs.configYaml.getString("messages.randomspawned") != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.configs.configYaml.getString("messages.randomspawned")));
            }
        }
    }
}
