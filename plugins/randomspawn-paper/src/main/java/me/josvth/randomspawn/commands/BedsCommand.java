package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.RespawnFlags;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;

public class BedsCommand extends AbstractCommand {

    public BedsCommand(RandomSpawn instance) {
        super(instance, "usebeds");
    }

    public boolean onCommand(CommandSender sender, List<String> args) {

        Player player = (Player) sender;
        String worldName = player.getWorld().getName();

        if (args.size() == 0) {
            if (this.plugin.configs.worlds.get(worldName) instanceof final WorldConfig worldConfig && worldConfig.randomSpawnOn().reviveWithBed()) {
                plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldName, WorldConfig.KEY_RANDOM_SPAWN_ON, RespawnFlags.KEY_REVIVE_WITH_BED), false);
                plugin.playerInfo((Player) sender, "Beds will now work like normal.");
                plugin.configs.saveWorldsFile();
                return true;
            } else {
                plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldName, WorldConfig.KEY_RANDOM_SPAWN_ON, RespawnFlags.KEY_REVIVE_WITH_BED), true);
                plugin.playerInfo((Player) sender, "Beds are now disabled.");
                plugin.configs.saveWorldsFile();
                return true;
            }
        }

        if (args.size() == 1) {
            if (args.get(0).matches("true")) {
                plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldName, WorldConfig.KEY_RANDOM_SPAWN_ON, RespawnFlags.KEY_REVIVE_WITH_BED), true);
                plugin.playerInfo((Player) sender, "Beds are now disabled.");
                plugin.configs.saveWorldsFile();
                return true;
            }
            if (args.get(0).matches("false")) {
                plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldName, WorldConfig.KEY_RANDOM_SPAWN_ON, RespawnFlags.KEY_REVIVE_WITH_BED), false);
                plugin.playerInfo((Player) sender, "Beds will now work like normal.");
                plugin.configs.saveWorldsFile();
                return true;
            }

        }
        return false;
    }
}
