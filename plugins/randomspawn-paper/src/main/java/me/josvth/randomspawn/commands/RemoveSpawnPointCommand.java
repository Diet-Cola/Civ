package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;

/**
 * This command removes a named spawnpoint from the config. It matches using the "name" field in the map of the spawn point.
 *
 * @author ProgrammerDan programmerdan@gmail.com
 */
public class RemoveSpawnPointCommand extends AbstractCommand {

    public RemoveSpawnPointCommand(RandomSpawn instance) {
        super(instance, "removespawn");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return false;
        }

        String name;
        String world = args.get(0);

        StringBuilder nameSB = new StringBuilder(args.get(1));
        for (int a = 2; a < args.size(); a++) {
            nameSB.append(" ").append(args.get(a));
        }
        name = nameSB.toString();

        if (!(this.plugin.configs.worlds.get(world) instanceof final WorldConfig worldConfig) || worldConfig.cities().isEmpty()) {
            sender.sendMessage("There are no spawn points configured for world " + world);
            return true;
        }

        ConfigurationSection current = plugin.configs.worldsYaml.getConfigurationSection(ConfigHelpers.pathOf(world, WorldConfig.KEY_CITY_SPAWNS));

        for (String key : current.getKeys(false)) {
            if (key.equalsIgnoreCase(name)) {
                current.set(key, null);

                plugin.configs.saveWorldsFile();
                sender.sendMessage("Removed spawn point by name of " + name);
                return true;
            }
        }

        sender.sendMessage("Unable to find a spawn point with that name");
        return true;
    }

}
