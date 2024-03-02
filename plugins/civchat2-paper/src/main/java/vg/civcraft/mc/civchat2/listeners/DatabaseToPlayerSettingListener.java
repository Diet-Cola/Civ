package vg.civcraft.mc.civchat2.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civchat2.utility.CivChat2SettingsManager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseToPlayerSettingListener implements Listener {

    private CivChat2SettingsManager settingsManager;

    public DatabaseToPlayerSettingListener(CivChat2SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        CivChatDAO db = CivChat2.getInstance().getDatabaseManager();
        UUID player = event.getPlayer().getUniqueId();

        List<UUID> ignoredPlayers = db.getIgnoredPlayers(player);
        List<String> ignoredGroups = db.getIgnoredGroups(player);

        this.settingsManager.setIgnoredPlayers(player, ignoredPlayers);

        List<Integer> groupIDs = new ArrayList<>();
        for (String s : ignoredGroups) {
            Group group = GroupManager.getGroup(s);
            if (group != null) {
                groupIDs.add(group.getGroupId());
            }
        }

        this.settingsManager.setIgnoredGroups(player, groupIDs);
    }
}
