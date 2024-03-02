package vg.civcraft.mc.civchat2.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.utility.CivChat2Config;

public class CivChatDAO {

	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Config config = plugin.getPluginConfig();
	private Database db;
	private HashMap<UUID, List<UUID>> ignoredPlayers = new HashMap<>();
	private HashMap<UUID, List<String>> ignoredGroups = new HashMap<>();
	private String getIgnoredPlayers;
	private String getIgnoredGroups;
	private String loadIgnoredPlayersList;
	private String loadIgnoredGroupsList;

	public CivChatDAO() {
		if (!isValidConnection()) {
			return;
		}
		executeDatabaseStatements();
		loadPreparedStatements();
		loadIgnoredPlayersList();
		loadIgnoredGroupsList();
	}

	public boolean isValidConnection() {
		String username = config.getMysqlUsername();
		String host = config.getMysqlHost();
		int port = config.getMysqlPort();
		String password = config.getMysqlPassword();
		String dbname = config.getMysqlDBname();
		db = new Database(host, port, dbname, username, password, plugin.getLogger());
		return db.connect();
	}

	private void executeDatabaseStatements() {
		db.execute("create table if not exists PlayersIgnoreList(" + "player varchar(36) not null,"
				+ "ignoredPlayer varchar(36) not null);");
		db.execute("create table if not exists GroupsIgnoreList(" + "player varchar(36) not null,"
				+ "ignoredGroup varchar(255) not null);");
	}

	private void loadPreparedStatements() {
		getIgnoredPlayers = "select * from PlayersIgnoreList where player = ?;";
		getIgnoredGroups = "select * from GroupsIgnoreList where player = ?;";
		loadIgnoredPlayersList = "select * from PlayersIgnoreList";
		loadIgnoredGroupsList = "select * from GroupsIgnoreList";
	}

	private boolean addIgnoredPlayerToMap(UUID playerUUID, UUID ignoredPlayerUUID) {
		if (ignoredPlayers.containsKey(playerUUID)) {
			if (ignoredPlayers.get(playerUUID).contains(ignoredPlayerUUID)) {
				return false;
			}
			ignoredPlayers.get(playerUUID).add(ignoredPlayerUUID);
		} else {
			List<UUID> ignoredPlayersList = new ArrayList<>();
			ignoredPlayersList.add(ignoredPlayerUUID);
			ignoredPlayers.put(playerUUID, ignoredPlayersList);
		}
		return true;
	}

	public void loadIgnoredPlayersList() {
		PreparedStatement loadIgnoredPlayersList = db.prepareStatement(this.loadIgnoredPlayersList);
		try {
			ResultSet rs = loadIgnoredPlayersList.executeQuery();
			while (rs.next()) {
				addIgnoredPlayerToMap(UUID.fromString(rs.getString("player")),
						UUID.fromString(rs.getString("ignoredPlayer")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean addIgnoredGroupToMap(UUID playerUUID, String group) {
		if (ignoredGroups.containsKey(playerUUID)) {
			if (ignoredGroups.get(playerUUID).contains(group)) {
				return false;
			}
			ignoredGroups.get(playerUUID).add(group);
		} else {
			List<String> ignoredGroupList = new ArrayList<String>();
			ignoredGroupList.add(group);
			ignoredGroups.put(playerUUID, ignoredGroupList);
		}
		return true;
	}

	public void loadIgnoredGroupsList() {
		PreparedStatement loadIgnoredGroupsList = db.prepareStatement(this.loadIgnoredGroupsList);
		try {
			ResultSet rs = loadIgnoredGroupsList.executeQuery();
			while (rs.next()) {
				addIgnoredGroupToMap(UUID.fromString(rs.getString("player")), rs.getString("ignoredGroup"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<UUID> getIgnoredPlayers(UUID playerUUID) {
		if (ignoredPlayers.containsKey(playerUUID)) {
			return ignoredPlayers.get(playerUUID);
		}
		List<UUID> ignoredPlayersList = new LinkedList<>();
		PreparedStatement getIgnoredPlayers = db.prepareStatement(this.getIgnoredPlayers);
		try {
			getIgnoredPlayers.setString(1, playerUUID.toString());
			ResultSet rs = getIgnoredPlayers.executeQuery();
			while (rs.next()) {
				ignoredPlayersList.add(UUID.fromString(rs.getString("ignoredPlayer")));
			}
			ignoredPlayers.put(playerUUID, ignoredPlayersList);
			return ignoredPlayersList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<String> getIgnoredGroups(UUID playerUUID) {
		if (ignoredGroups.containsKey(playerUUID)) {
			return ignoredGroups.get(playerUUID);
		}
		List<String> ignoredGroupsList = new LinkedList<String>();
		PreparedStatement getIgnoredGroups = db.prepareStatement(this.getIgnoredGroups);
		try {
			getIgnoredGroups.setString(1, playerUUID.toString());
			ResultSet rs = getIgnoredGroups.executeQuery();
			while (rs.next()) {
				ignoredGroupsList.add(rs.getString("group"));
			}
			ignoredGroups.put(playerUUID, ignoredGroupsList);
			return ignoredGroupsList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
