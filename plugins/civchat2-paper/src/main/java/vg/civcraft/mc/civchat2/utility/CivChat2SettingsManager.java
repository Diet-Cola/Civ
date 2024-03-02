package vg.civcraft.mc.civchat2.utility;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.EnumSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.LongSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.StringSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.collection.ListSetting;

public class CivChat2SettingsManager {

	private BooleanSetting showJoins;
	private BooleanSetting showLeaves;
	private BooleanSetting sendOwnKills;
	private BooleanSetting receiveKills;
	private BooleanSetting receiveKillsFromIgnoredPlayers;
	private BooleanSetting showChatGroup;
	private DisplayLocationSetting chatGroupLocation;
	private LongSetting chatUnmuteTimer;
	private EnumSetting<KillMessageFormat> killMessageFormat;
	private ListSetting<UUID> ignoredPlayers;
	private ListSetting<Integer> ignoredGroups;

	public CivChat2SettingsManager() {
		initSettings();
	}

	private void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("CivChat",
				"All options related to CivChat.", new ItemStack(Material.OAK_SIGN));

		showJoins = new BooleanSetting(CivChat2.getInstance(), true, "Show Player Joins", "showJoins",
				"Should player join messages be shown?");
		PlayerSettingAPI.registerSetting(showJoins, menu);

		showLeaves = new BooleanSetting(CivChat2.getInstance(), true, "Show Players Leaving", "showLeaves",
				"Should player leave messages be shown?");
		PlayerSettingAPI.registerSetting(showLeaves, menu);

		sendOwnKills = new BooleanSetting(CivChat2.getInstance(), true, "Broadcast your kills", "civChatBroadcastKills",
				"Should kills you make be broadcasted to nearby players?");
		PlayerSettingAPI.registerSetting(sendOwnKills, menu);

		receiveKills = new BooleanSetting(CivChat2.getInstance(), true, "Receive kill broadcasts",
				"civChatReceiveKills", "Do you want to receive broadcasts for nearby kills");
		PlayerSettingAPI.registerSetting(receiveKills, menu);

		receiveKillsFromIgnoredPlayers = new BooleanSetting(CivChat2.getInstance(), false,
				"Receive kill broadcasts from ignored players", "civChatReceiveKillsIgnored",
				"Do you want to receive kill broadcasts from killers you have ignored");
		PlayerSettingAPI.registerSetting(receiveKillsFromIgnoredPlayers, menu);

		showChatGroup = new BooleanSetting(CivChat2.getInstance(), true, "Show current chat group", "showChatGroup",
				"Should player chat group be shown?");
		PlayerSettingAPI.registerSetting(showChatGroup, menu);

		chatGroupLocation = new DisplayLocationSetting(CivChat2.getInstance(), DisplayLocationSetting.DisplayLocation.SIDEBAR,
				"Chat Group Location", "chatGroupLocation", new ItemStack(Material.ARROW), "the current chat group");
		PlayerSettingAPI.registerSetting(chatGroupLocation, menu);
		
		chatUnmuteTimer = new LongSetting(CivChat2.getInstance(), 0L, "Global chat mute", "chatGlobalMuteTimer");
		PlayerSettingAPI.registerSetting(chatUnmuteTimer, null);

		killMessageFormat = new EnumSetting<>(CivChat2.getInstance(), KillMessageFormat.WITH, "Kill Message Format", "killMessageFormat", new ItemStack(Material.WRITABLE_BOOK), "Choose your kill message format", true, KillMessageFormat.class);
		PlayerSettingAPI.registerSetting(killMessageFormat, menu);

		ignoredPlayers = new ListSetting<>(CivChat2.getInstance(), "civChatIgnoredPlayers", "civChatIgnoredPlayers", null, "Ignored Players", UUID.class);
		PlayerSettingAPI.registerSetting(ignoredPlayers, null);

		ignoredGroups = new ListSetting<>(CivChat2.getInstance(), "civChatIgnoredGroups", "civChatIgnoredGroups", null, "Ignored Groups", Integer.class);
		PlayerSettingAPI.registerSetting(ignoredGroups, null);
	}
	
	public LongSetting getGlobalChatMuteSetting() {
		return chatUnmuteTimer;
	}

	public boolean getShowJoins(UUID uuid) {
		return showJoins.getValue(uuid);
	}

	public boolean getShowLeaves(UUID uuid) {
		return showLeaves.getValue(uuid);
	}
	
	public boolean getSendOwnKills(UUID uuid) {
		return sendOwnKills.getValue(uuid);
	}
	
	public boolean getReceiveKills(UUID uuid) {
		return receiveKills.getValue(uuid);
	}
	
	public boolean getReceiveKillsFromIgnored(UUID uuid) {
		return receiveKillsFromIgnoredPlayers.getValue(uuid);
	}

	public boolean getShowChatGroup(UUID uuid) {
		return showChatGroup.getValue(uuid);
	}

	public DisplayLocationSetting getChatGroupLocation() {
		return chatGroupLocation;
	}

	public KillMessageFormat getKillMessageFormat(UUID uuid) {
		return killMessageFormat.getValue(uuid);
	}

	public List<UUID> getIgnoredPlayers(UUID uuid) {return ignoredPlayers.getValue(uuid);}

	public List<Integer> getIgnoredGroups(UUID uuid) {return ignoredGroups.getValue(uuid);}

	public void setIgnoredPlayers(UUID player, List<UUID> ignoredPlayers) {
		this.ignoredPlayers.setValue(player, ignoredPlayers);
	}

	/**
	 * Modifies a players ignored player list
	 * @param player Player we are modifying the ignoring player list of
	 * @param ignoredPlayer Target player UUID we are ignoring
	 * @param adding Flag to determine whether adding or removing from the list, True for adding to list, false for removing from ignoring list
	 */
	public void modifyIgnoredPlayer(UUID player, UUID ignoredPlayer, boolean adding) {
		List<UUID> tempIgnoredPlayers = getIgnoredPlayers(player);
		if (!tempIgnoredPlayers.contains(ignoredPlayer)) {
			return;
		}
		if (adding) {
			tempIgnoredPlayers.add(ignoredPlayer);
			setIgnoredPlayers(player, tempIgnoredPlayers);
			return;
		}
		tempIgnoredPlayers.remove(ignoredPlayer);
		setIgnoredPlayers(player, tempIgnoredPlayers);
	}

	public void setIgnoredGroups(UUID player, List<Integer> ignoredGroups) {
		this.ignoredGroups.setValue(player, ignoredGroups);
	}

	/**
	 * Modifies a players ignored group list
	 * @param player Player we are modifying the ignoring player list of
	 * @param ignoredGroup Target group id we are ignoring
	 * @param adding Flag to determine whether adding or removing from the list, True for adding to list, false for removing from ignoring list
	 */
	public void modifyIgnoredGroups(UUID player, Integer ignoredGroup, boolean adding) {
		List<Integer> tempIgnoredGroups = getIgnoredGroups(player);
		if (!tempIgnoredGroups.contains(ignoredGroup)) {
			return;
		}
		if (adding) {
			tempIgnoredGroups.add(ignoredGroup);
			setIgnoredGroups(player, tempIgnoredGroups);
			return;
		}
		tempIgnoredGroups.remove(ignoredGroup);
		setIgnoredGroups(player, tempIgnoredGroups);
	}

	public boolean isIgnoringPlayer(UUID player, UUID ignoredPlayer) {
		return getIgnoredPlayers(player).contains(ignoredPlayer);
	}

	public boolean isIgnoringGroup(UUID player, Integer groupID){
		return getIgnoredGroups(player).contains(groupID);
	}


	public enum KillMessageFormat {
		FOR(
			"for"
			),
		WHILE(
			"while"
		),
		BLANK(
			""
		),
		USING(
			"using"
		),
		BY(
			"by"
		),
		WITH(
			"with"
		);

		public final String simpleDescription;

		private KillMessageFormat(String simpleDescription) {
			this.simpleDescription = simpleDescription;
		}	
	}
}
