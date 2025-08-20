package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.FirstSpawn;
import me.josvth.randomspawn.config.worlds.RespawnFlags;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;

public class SetFirstSpawnCommand extends AbstractCommand {

    public SetFirstSpawnCommand(RandomSpawn instance) {
        super(instance, "setfirstspawn");
    }

    public boolean onCommand(CommandSender sender, List<String> args) {
        Player player = (Player) sender;

        String worldname = player.getWorld().getName();

        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();

        double yaw = (double) player.getLocation().getYaw();
        double pitch = (double) player.getLocation().getPitch();

        plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_FIRST_SPAWN, FirstSpawn.KEY_X), x);
        plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_FIRST_SPAWN, FirstSpawn.KEY_Y), y);
        plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_FIRST_SPAWN, FirstSpawn.KEY_Z), z);
        plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_FIRST_SPAWN, FirstSpawn.KEY_YAW), yaw);
        plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_FIRST_SPAWN, FirstSpawn.KEY_PITCH), pitch);

        plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_RANDOM_SPAWN_ON, RespawnFlags.KEY_FIRST_JOIN), false);
        plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_POINT_ON, RespawnFlags.KEY_FIRST_JOIN), false);

        plugin.configs.saveWorldsFile();

        plugin.playerInfo(player, "First spawn location set!");
        plugin.playerInfo(player, "Random spawning on first join is now disabled!");

        return true;
    }
}
