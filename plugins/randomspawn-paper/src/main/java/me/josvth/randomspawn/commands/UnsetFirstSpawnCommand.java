package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;

public class UnsetFirstSpawnCommand extends AbstractCommand {

    public UnsetFirstSpawnCommand(RandomSpawn instance) {
        super(instance, "unsetfirstspawn");
    }

    public boolean onCommand(CommandSender sender, List<String> args) {
        Player player = (Player) sender;
        String worldname = player.getWorld().getName();
        if (ConfigHelpers.remove(plugin.configs.worldsYaml, ConfigHelpers.pathOf(worldname, WorldConfig.KEY_FIRST_SPAWN))) {
            plugin.configs.saveWorldsFile();

            plugin.playerInfo(player, "The first spawn location of this world is removed!");
            plugin.playerInfo(player, "Now refering to world spawn.");
        } else {
            plugin.playerInfo(player, "There's no first spawnpoint set in this world!");
        }
        return true;
    }
}
