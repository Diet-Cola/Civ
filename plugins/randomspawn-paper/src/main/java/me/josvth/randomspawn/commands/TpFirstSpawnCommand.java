package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.FirstSpawn;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpFirstSpawnCommand extends AbstractCommand {

    public TpFirstSpawnCommand(RandomSpawn instance) {
        super(instance, "tpfirstspawn");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {

        Player player = (Player) sender;
        String worldname = player.getWorld().getName();

        if (this.plugin.configs.worlds.get(worldname) instanceof final WorldConfig worldConfig && worldConfig.firstSpawn() instanceof FirstSpawn(
			final int x,
            final int y,
            final int z,
            final float yaw,
            final float pitch
		)) {
            player.teleportAsync(new Location(player.getWorld(), x, y, z, yaw, pitch));
            plugin.playerInfo(player, "You've been teleported to the first spawn location of this world!");
        } else {
            plugin.playerInfo(player, "There's no first spawnpoint set in this world!");
        }
        return true;
    }
}
