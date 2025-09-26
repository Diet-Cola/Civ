package me.josvth.randomspawn;

import me.josvth.randomspawn.config.Configs;
import me.josvth.randomspawn.handlers.CommandHandler;
import me.josvth.randomspawn.listeners.DamageListener;
import me.josvth.randomspawn.listeners.JoinListener;
import me.josvth.randomspawn.listeners.RespawnListener;
import me.josvth.randomspawn.listeners.SignListener;
import me.josvth.randomspawn.listeners.WorldChangeListener;
import me.josvth.randomspawn.spawn.CachedSpawnSelector;
import me.josvth.randomspawn.spawn.SpawnSelector;
import org.apache.commons.lang3.function.Failable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.ACivMod;

public final class RandomSpawn extends ACivMod {
    public Configs configs;
    CommandHandler commandHandler;
    RespawnListener respawnListener;
    JoinListener joinListener;
    WorldChangeListener worldChangeListener;
    SignListener signListener;
    DamageListener damageListener;

    private SpawnSelector spawnSelector;

    @Override
    public void onEnable() {
        saveResource("config.yml", false);
        saveResource("worlds.yml", false);

        // setup handlers
        this.configs = new Configs(this);
        Failable.run(() -> {
            this.configs.loadConfigFile();
            this.configs.loadWorldsFile();
        });
        logDebug("Yamls loaded!");

        commandHandler = new CommandHandler(this);
        logDebug("Commands registered!");

        // setup listeners
        respawnListener = new RespawnListener(this);
        joinListener = new JoinListener(this);
        worldChangeListener = new WorldChangeListener(this);
        signListener = new SignListener(this);
        damageListener = new DamageListener(this);

        this.spawnSelector = new CachedSpawnSelector(this);
    }

    public void logInfo(String message) {
        getLogger().info(message);
    }

    public void logDebug(String message) {
        if (configs.config.debug()) {
            getLogger().info("(DEBUG) " + message);
        }
    }

    public void logWarning(String message) {
        getLogger().warning(message);
    }

    public void playerInfo(Player player, String message) {
        player.sendMessage(ChatColor.AQUA + "[RandomSpawn] " + ChatColor.RESET + message);
    }

    // *------------------------------------------------------------------------------------------------------------*
    // | The following chooseSpawn methods contains code made by NuclearW |
    // | based on his SpawnArea plugin: |
    // |
    // http://forums.bukkit.org/threads/tp-spawnarea-v0-1-spawns-targetPlayers-in-a-set-area-randomly-1060.20408/
    // |
    // *------------------------------------------------------------------------------------------------------------*

    public SpawnSelector getSpawnSelector() {
        return spawnSelector;
    }

    @Deprecated
    public Location chooseSpawn(World to) {
        return spawnSelector.getRandomSpawn(to);
    }
}
