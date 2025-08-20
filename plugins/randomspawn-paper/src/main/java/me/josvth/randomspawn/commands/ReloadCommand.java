package me.josvth.randomspawn.commands;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import org.apache.commons.lang3.function.Failable;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends AbstractCommand {

    public ReloadCommand(RandomSpawn instance) {
        super(instance, "reload");
    }

    public boolean onCommand(CommandSender sender, List<String> args) {

        if (args.size() == 0) {
            Failable.run(plugin.configs::loadConfigFile);
            Failable.run(plugin.configs::loadWorldsFile);
            sender.sendMessage("Random Spawn configurations reloaded!");
            return true;
        }

        if (args.get(0).matches("config")) {
            Failable.run(plugin.configs::loadConfigFile);
            sender.sendMessage("Random Spawn config file is reloaded!");
            return true;
        }

        if (args.get(0).matches("worlds")) {
            Failable.run(plugin.configs::loadWorldsFile);
            sender.sendMessage("Random Spawn worlds file reloaded!");
            return true;
        }

        return false;
    }
}
