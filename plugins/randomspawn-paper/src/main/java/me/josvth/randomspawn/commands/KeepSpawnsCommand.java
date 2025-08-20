package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;

public class KeepSpawnsCommand extends AbstractCommand {

    public KeepSpawnsCommand(RandomSpawn instance) {
        super(instance, "keepfirstspawns");
    }

    public boolean onCommand(CommandSender sender, List<String> args) {

        Player player = (Player) sender;
        String worldname = player.getWorld().getName();

        if (args.size() == 0) {
            if (plugin.configs.worlds.get(worldname) instanceof final WorldConfig config && config.keepRandomSpawn()) {
                plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_KEEP_RANDOM_SPAWN), false);
                plugin.playerInfo(player, "Keep random spawns is now disabled.");
                plugin.configs.saveWorldsFile();
                return true;
            } else {
                plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_KEEP_RANDOM_SPAWN), true);
                plugin.playerInfo(player, "Random Spawn will now save the spawn locations.");
                plugin.configs.saveWorldsFile();
                return true;
            }
        }
        if (args.size() == 1) {
            if (args.get(0).matches("true")) {
                plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_KEEP_RANDOM_SPAWN), true);
                plugin.playerInfo(player, "Random Spawn will now save the spawn locations.");
                plugin.configs.saveWorldsFile();
                return true;
            }
            if (args.get(0).matches("false")) {
                plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_KEEP_RANDOM_SPAWN), false);
                plugin.playerInfo(player, "Keep random spawns is now disabled.");
                plugin.configs.saveWorldsFile();
                return true;
            }
        }

        return false;
    }
}
