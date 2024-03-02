package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civchat2.utility.CivChat2SettingsManager;

import java.util.List;
import java.util.UUID;

public class Ignore extends BaseCommand {

	@CommandAlias("ignore")
	@Syntax("<player>")
	@Description("Toggles ignoring a player")
	@CommandCompletion("@allplayers")
	public void execute(Player player, String targetPlayer) {
		Player ignoredPlayer = Bukkit.getServer().getPlayer(targetPlayer);
		if (ignoredPlayer == null) {
			player.sendMessage(ChatStrings.chatPlayerNotFound);
			return;
		}
		if (player == ignoredPlayer) {
			player.sendMessage(ChatStrings.chatCantIgnoreSelf);
			return;
		}
		CivChat2SettingsManager settingsManager = CivChat2.getInstance().getCivChat2SettingsManager();
		List<UUID> ignoredPlayers = settingsManager.getIgnoredPlayers(player.getUniqueId());
		if (!ignoredPlayers.contains(ignoredPlayer.getName())) {
			// Player added to the list
			settingsManager.modifyIgnoredPlayer(player.getUniqueId(), ignoredPlayer.getUniqueId(), true);
			player.sendMessage(String.format(ChatStrings.chatNowIgnoring, ignoredPlayer.getDisplayName()));
			return;
		} else {
			// Player removed from the list
			settingsManager.modifyIgnoredPlayer(player.getUniqueId(), ignoredPlayer.getUniqueId(), false);
			player.sendMessage(String.format(ChatStrings.chatStoppedIgnoring, ignoredPlayer.getDisplayName()));
			return;
		}
	}
}
