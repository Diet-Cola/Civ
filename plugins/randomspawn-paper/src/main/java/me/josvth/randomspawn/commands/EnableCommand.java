package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.RandomSpawnArea;
import me.josvth.randomspawn.config.worlds.RespawnFlags;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;

public class EnableCommand extends AbstractCommand {

    public EnableCommand(RandomSpawn instance) {
        super(instance, "enable");
    }

    public boolean onCommand(CommandSender sender, List<String> args) {
        Player player = (Player) sender;
        String worldname = player.getWorld().getName();

        plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_RANDOM_SPAWN_ON, RespawnFlags.KEY_REVIVE_WITHOUT_BED), true);

        if (!(plugin.configs.worldsYaml.contains(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA)))) {
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_TYPE), RandomSpawnArea.KEY_SQUARE_TYPE);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MIN_X), -100);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MAX_X), 100);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MIN_Z), -100);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MAX_Z), 100);
        }

        plugin.configs.saveWorldsFile();
        plugin.playerInfo(player, "Random Spawn is now enabled in this world!");

        return true;
    }
}
