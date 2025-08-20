package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;

public class DisableCommand extends AbstractCommand {

    public DisableCommand(RandomSpawn instance) {
        super(instance, "disable");
    }

    public boolean onCommand(CommandSender sender, List<String> args) {
        Player player = (Player) sender;
        String worldname = player.getWorld().getName();
        plugin.configs.worldsYaml.set(ConfigHelpers.pathOf(worldname, WorldConfig.KEY_RANDOM_SPAWN_ON), null);
        plugin.configs.saveWorldsFile();
        plugin.playerInfo(player, "Random Spawn is now disabled in this world!");
        return true;
    }
}
