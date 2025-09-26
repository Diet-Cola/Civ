package me.josvth.randomspawn.listeners;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.RandomSpawnUtils;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;

public class JoinListener implements Listener {
    private final RandomSpawn plugin;

    public JoinListener(
        final @NotNull RandomSpawn plugin
    ) {
        this.plugin = Objects.requireNonNull(plugin);
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void on(
        final @NotNull PlayerJoinEvent event
    ) {
        final Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            return;
        }

        final World world = player.getWorld();
        switch (world.getEnvironment()) {
            case NETHER:
            case THE_END:
                return;
        }

        final String worldName = world.getName();
        final WorldConfig worldConfig = this.plugin.configs.worlds.get(worldName);
        if (worldConfig == null) {
            return;
        }

        if (player.hasPermission("RandomSpawn.exclude")) {
            this.plugin.logDebug(player.getName() + " is excluded from Random Spawning.");
            return;
        }

        if (worldConfig.spawnPointOn().firstJoin()) {
            this.plugin.logDebug(player.getName() + " is first-join spawning at a spawn point");
            final Location spawn = this.plugin.getSpawnSelector().getSpawnPoint(world);
            if (spawn != null) {
                player.teleportAsync(spawn);
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

        if (worldConfig.randomSpawnOn().firstJoin()) {
            this.plugin.logDebug(player.getName() + " is first-join spawning randomly");
            final Location spawn = this.plugin.getSpawnSelector().getRandomSpawn(world);
            if (spawn == null) {
                this.plugin.logDebug(player.getName() + " got unlucky and was not successfully random spawned. Default behavior will apply.");
                return;
            }
            player.teleportAsync(spawn);
            RandomSpawnUtils.setLastTimeRandomSpawned(this.plugin, player, System.currentTimeMillis());
            if (worldConfig.keepRandomSpawn()) {
                player.setRespawnLocation(spawn);
            }
            if (plugin.configs.configYaml.getString("messages.randomspawned") instanceof final String message) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
            return;
        }

        // neither spawnpoints nor randomspawn were successful (or both were disabled)
        player.teleportAsync(worldConfig.getFirstSpawn(world));
        this.plugin.logDebug(player.getName() + " is teleported to the first spawn of " + worldName);
    }

    @EventHandler
    public void on(
        final @NotNull PlayerKickEvent event
    ) {
        final Long timestamp = RandomSpawnUtils.getLastTimeRandomSpawned(event.getPlayer());
        if (timestamp != null) {
            if (timestamp + TimeUnit.SECONDS.toMillis(plugin.configs.config.damageImmunityPeriod()) > System.currentTimeMillis()) {
                event.setReason("");
                event.setLeaveMessage("");
                event.setCancelled(true);
            }
        }
    }
}
