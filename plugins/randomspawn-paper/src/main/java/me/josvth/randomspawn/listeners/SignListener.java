package me.josvth.randomspawn.listeners;

import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.RandomSpawnUtils;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {

    private RandomSpawn plugin;

    public SignListener(RandomSpawn plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerSignInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Material mat = event.getClickedBlock().getType();
            if (Tag.WALL_SIGNS.isTagged(mat)) {
                Sign sign = (Sign) event.getClickedBlock().getState();
                final Player player = event.getPlayer();
                if (sign.getLine(0).equalsIgnoreCase(plugin.configs.config.signText())) {

                    if (player.hasPermission("RandomSpawn.usesign")) {

                        World world = null;

                        String worldName = sign.getLine(1);

                        if (worldName != null)
                            world = Bukkit.getWorld(worldName);

                        if (world == null)
                            world = player.getWorld();

                        final Location spawnLocation = plugin.chooseSpawn(world);

                        if (spawnLocation == null) {
                            plugin.logDebug(player.getName() + " got unlucky and was not successfully randomspawned. Default behavior will apply");
                            return;
                        }

                        player.teleportAsync(spawnLocation);

                        RandomSpawnUtils.setLastTimeRandomSpawned(this.plugin, player, System.currentTimeMillis());

                        WorldConfig config = plugin.configs.worlds.get(worldName);
                        if (config == null) {
                            plugin.logDebug("WorldConfig couldn't be found for world: " + worldName);
                            return;
                        }

                        if (config.keepRandomSpawn()) {
                            player.setBedSpawnLocation(spawnLocation);
                        }

                        if (plugin.configs.configYaml.getString("messages.randomspawned") instanceof final String message) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                        }

                    } else {
                        plugin.playerInfo(player, "You don't have the permission to use this Random Spawn Sign!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSignPlace(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase(plugin.configs.config.signText())) {
            Player player = event.getPlayer();
            if (player.hasPermission("RandomSpawn.placesign")) {
                this.plugin.playerInfo(player, "Random Spawn Sign created!");
            } else {
                event.setLine(0, "");
                this.plugin.playerInfo(player, "You don't have the permission to place a Random Spawn Sign!");
            }
        }
    }

}
