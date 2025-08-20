package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.RandomSpawnArea;
import me.josvth.randomspawn.config.worlds.RespawnFlags;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;

public class SetAreaCommand extends AbstractCommand {

    public SetAreaCommand(RandomSpawn instance) {
        super(instance, "setarea");
    }

    public boolean onCommand(CommandSender sender, List<String> args) {
        Player player = (Player) sender;

        double xmin = 0;
        double xmax = 0;
        double zmin = 0;
        double zmax = 0;

        if (args.size() == 1) {
            Location reference = player.getLocation();

            try {
                xmin = reference.getX() - Double.parseDouble(args.get(0));
                xmax = reference.getX() + Double.parseDouble(args.get(0));
                zmin = reference.getZ() - Double.parseDouble(args.get(0));
                zmax = reference.getZ() + Double.parseDouble(args.get(0));
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid number.");
                return false;
            }

            String worldname = reference.getWorld().getName();

            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MIN_X), xmin);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MAX_X), xmax);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MIN_Z), zmin);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MAX_Z), zmax);

            // TODO: What?
            plugin.configs.worldsYaml.set(worldname + ".randomspawnenabled", true);

            plugin.configs.saveWorldsFile();

            plugin.playerInfo(player, "Spawn area set!");

            return true;
        }

        if (args.size() == 2 && (args.get(0).equalsIgnoreCase("circle") || args.get(0).equalsIgnoreCase("square"))) {

            Location reference = player.getLocation();

            try {

                xmin = reference.getX() - Double.parseDouble(args.get(1));
                xmax = reference.getX() + Double.parseDouble(args.get(1));
                zmin = reference.getZ() - Double.parseDouble(args.get(1));
                zmax = reference.getZ() + Double.parseDouble(args.get(1));

            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid number.");
                return false;
            }

            String worldname = reference.getWorld().getName();

            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MIN_X), xmin);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MAX_X), xmax);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MIN_Z), zmin);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MAX_Z), zmax);

            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_RANDOM_SPAWN_ON, RespawnFlags.KEY_REVIVE_WITHOUT_BED), true);

            if (args.get(0).matches("circle")) {
                plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_TYPE), RandomSpawnArea.KEY_CIRCLE_TYPE);
            } else {
                plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_TYPE), RandomSpawnArea.KEY_SQUARE_TYPE);
            }

            plugin.playerInfo(player, "Spawn area set!");

            plugin.configs.saveWorldsFile();

            return true;
        }

        if (args.size() == 2) {

            Location reference = player.getLocation();

            try {
                xmin = reference.getX() - (Double.parseDouble(args.get(0)) / 2);
                xmax = reference.getX() + (Double.parseDouble(args.get(0)) / 2);
                zmin = reference.getZ() - (Double.parseDouble(args.get(1)) / 2);
                zmax = reference.getZ() + (Double.parseDouble(args.get(1)) / 2);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid number.");
                return false;
            }


            String worldname = reference.getWorld().getName();

            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MIN_X), xmin);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MAX_X), xmax);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MIN_Z), zmin);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MAX_Z), zmax);

            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_RANDOM_SPAWN_ON, RespawnFlags.KEY_REVIVE_WITHOUT_BED), true);

            plugin.configs.saveWorldsFile();

            plugin.playerInfo(player, "Spawn area set!");

            return true;
        }

        if (args.size() == 4) {
            Location reference = player.getLocation();

            try {

                xmin = Double.parseDouble(args.get(0));
                xmax = Double.parseDouble(args.get(1));
                zmin = Double.parseDouble(args.get(2));
                zmax = Double.parseDouble(args.get(3));

            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid number.");
                return false;
            }

            String worldname = reference.getWorld().getName();

            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MIN_X), xmin);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MAX_X), xmax);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MIN_Z), zmin);
            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_SPAWN_AREA, RandomSpawnArea.KEY_SQUARE_MAX_Z), zmax);

            plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_RANDOM_SPAWN_ON, RespawnFlags.KEY_REVIVE_WITHOUT_BED), true);

            plugin.configs.saveWorldsFile();

            plugin.playerInfo(player, "Spawn area set!");

            return true;
        }

        return false;

    }

}
