package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand extends AbstractCommand {

    public InfoCommand(RandomSpawn instance) {
        super(instance, "info");
    }

    public boolean onCommand(CommandSender sender, List<String> args) {

        Player player = (Player) sender;
        String worldname = player.getWorld().getName();

        player.sendMessage(ChatColor.WHITE + " ---------------- " + ChatColor.AQUA + "Random Spawn Info" + ChatColor.WHITE + " ------------------ ");
        player.sendMessage(ChatColor.AQUA + "World: " + ChatColor.WHITE + worldname);

        final WorldConfig worldConfig = this.plugin.configs.worlds.get(worldname);
        if (worldConfig == null) {
            player.sendMessage("Is not configured in Random Spawn ");
            player.sendMessage(ChatColor.WHITE + " --------------------------------------------------- ");
            return true;
        }

        player.sendMessage(Component.text(ToStringBuilder.reflectionToString(worldConfig, ToStringStyle.JSON_STYLE)));
        player.sendMessage(ChatColor.WHITE + " --------------------------------------------------- ");

        return true;
    }
}
