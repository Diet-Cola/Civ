package me.josvth.randomspawn.listeners;

import java.util.Random;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.RandomSpawnUtils;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {

    private RandomSpawn plugin;
    private Random rng;

    public RespawnListener(RandomSpawn plugin) {
        this.plugin = plugin;
        this.rng = new Random();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (player.hasPermission("RandomSpawn.exclude")) {
            plugin.logDebug(playerName + " is excluded from Random Spawning.");
            return;
        }

        World world = event.getRespawnLocation().getWorld();
        String worldName = world.getName();

        final WorldConfig worldConfig = this.plugin.configs.worlds.get(worldName);
        if (worldConfig == null) {
            return;
        }

        if (event.isBedSpawn() && worldConfig.randomSpawnOn().reviveWithBed()) {
            this.plugin.logDebug(playerName + " is spawned at his bed!");
            return;
        }

        final Location bedLocation = player.getRespawnLocation();
        if (worldConfig.keepRandomSpawn() && bedLocation != null) {
            event.setRespawnLocation(bedLocation);
            this.plugin.logDebug(playerName + " is spawned at their saved spawn.");
            return;
        }

        if (worldConfig.spawnPointOn().reviveWhileNew()) {
            if (player.getFirstPlayed() + worldConfig.newPlayerPeriod() > System.currentTimeMillis()) {
                this.plugin.logDebug(playerName + " newplayer respawn using Spawn Points");
                // still a new player, continue.
                final Location spawn = this.plugin.getSpawnSelector().getRandomSpawn(world);
                if (spawn != null) {
                    event.setRespawnLocation(spawn);
                    RandomSpawnUtils.setLastTimeRandomSpawned(this.plugin, player, System.currentTimeMillis());

                    if (worldConfig.keepRandomSpawn()) {
                        player.setRespawnLocation(spawn);
                    }

                    if (plugin.configs.configYaml.getString("messages.randomspawned") instanceof final String message) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    }
                    return;
                }
            }
        }

        if (worldConfig.randomSpawnOn().reviveWithoutBed()) {
            this.plugin.logDebug(playerName + " standard Respawn");

            final Location spawn = this.plugin.getSpawnSelector().getRandomSpawn(world);

            if (spawn == null) {
                plugin.logDebug(playerName + " got unlucky and was not successfully randomspawned. Default behavior will apply");
                return;
            }

            event.setRespawnLocation(spawn);

            RandomSpawnUtils.setLastTimeRandomSpawned(this.plugin, player, System.currentTimeMillis());

            if (worldConfig.keepRandomSpawn()) {
                player.setRespawnLocation(spawn);
            }

            if (plugin.configs.configYaml.getString("messages.randomspawned") instanceof final String message) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }
}
