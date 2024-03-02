package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civchat2.utility.CivChat2SettingsManager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

import java.util.List;

public class IgnoreGroup extends BaseCommand {

	@CommandAlias("ignoregroup")
	@Syntax("<group>")
	@Description("Toggles ignoring a group")
	public void execute(Player player, String targetGroup) {
		Group group = GroupManager.getGroup(targetGroup);
		if (group == null) {
			player.sendMessage(ChatStrings.chatGroupNotFound);
			return;
		}

		CivChat2SettingsManager settingsManager = CivChat2.getInstance().getCivChat2SettingsManager();
		List<Integer> ignoredGroups = settingsManager.getIgnoredGroups(player.getUniqueId());
		int groupID = group.getGroupId();
		if (ignoredGroups.contains(groupID)) {
			settingsManager.modifyIgnoredGroups(player.getUniqueId(), groupID, false);
			player.sendMessage(String.format(ChatStrings.chatStoppedIgnoring, group.getName()));
		} else {
			settingsManager.modifyIgnoredGroups(player.getUniqueId(), group.getGroupId(), true);
			CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
			player.sendMessage(String.format(ChatStrings.chatNowIgnoring, group.getName()));
			if (group.equals(chatMan.getGroupChatting(player))) {
				chatMan.removeGroupChat(player);
				player.sendMessage(ChatStrings.chatMovedToGlobal);
			}
		}
	}
}
