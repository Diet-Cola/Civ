package com.github.maxopoly.finale.misc.ally;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ParticleUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class AllyHandler implements Listener {

    private boolean enabled;
    private boolean seeInvisAlly;
    private boolean animateLinkedEnabled;
    private double animateLinkedMaxDistance;
    private Map<UUID, Set<UUID>> playerAllies = new HashMap<>();

    private final SQLite sqlite;
    private Connection connection;

    public AllyHandler(boolean enabled, boolean seeInvisAlly, boolean animateLinkedEnabled, double animateLinkedMaxDistance, SQLite sqlite) {
        this.enabled = enabled;
        if (CivModCorePlugin.isFolia()) {
            this.enabled = false;
        }
        this.seeInvisAlly = seeInvisAlly;
        this.animateLinkedEnabled = animateLinkedEnabled;
        this.animateLinkedMaxDistance = animateLinkedMaxDistance;
        this.sqlite = sqlite;
        Bukkit.getPluginManager().registerEvents(this, Finale.getPlugin());
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Finale.getPlugin(), task -> {
            for (Map.Entry<UUID, Set<UUID>> playerAlliesEntry : playerAllies.entrySet()) {
                Player player = Bukkit.getPlayer(playerAlliesEntry.getKey());
                if (player == null) continue;
                if (!player.isOnline()) continue;

                Team allyTeam = getAllyTeam(player);
                for (UUID allyID : playerAlliesEntry.getValue()) {
                    Player ally = Bukkit.getPlayer(allyID);
                    if (ally.isOnline()) {
                        allyTeam.addEntry(ally.getName());
                    }
                }
            }
        }, 1L, 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (enabled) {
            getAllies(event.getPlayer());
        }
    }

    public void init() {
        connection = sqlite.open();
        try {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Finale.getPlugin(), task -> {
            if (CivModCorePlugin.isFolia()) {
                return;
            }
            for (Map.Entry<UUID, Set<UUID>> entry : playerAllies.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                Set<UUID> allyIDs = entry.getValue();
                Iterator<UUID> allyIDIterator = allyIDs.iterator();
                while (allyIDIterator.hasNext()) {
                    UUID allyID = allyIDIterator.next();
                    Player ally = Bukkit.getPlayer(allyID);
                    if (ally != null && ally.isOnline()) {
                        Team allyTeam = getAllyTeam(player);
                        allyTeam.addEntry(ally.getName());
                    }
                }
            }
        }, 1L, 1L);
    }

    public void shutdown() {
        sqlite.close();
        connection = null;
    }

    private void createTables(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ALLY(" +
            "PlayerUUID TEXT," +
            "AllyUUID TEXT," +
            "PRIMARY KEY (PlayerUUID, AllyUUID)" +
            ")");
        preparedStatement.execute();
    }

    public void save() {
        if (!enabled) {
            return;
        }

        try {
            for (Map.Entry<UUID, Set<UUID>> allyEntry : playerAllies.entrySet()) {
                PreparedStatement clearStatement = connection.prepareStatement("DELETE FROM ALLY WHERE PlayerUUID = ?");
                clearStatement.setString(1, allyEntry.getKey().toString());
                clearStatement.execute();

                for (UUID allyUUID : allyEntry.getValue()) {
                    PreparedStatement insertAlly = connection.prepareStatement("INSERT INTO ALLY(" +
                        "PlayerUUID, AllyUUID" +
                        ") VALUES (" +
                        "?, ?" +
                        ")");
                    insertAlly.setString(1, allyEntry.getKey().toString());
                    insertAlly.setString(2, allyUUID.toString());
                    insertAlly.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSeeInvisAlly() {
        return seeInvisAlly;
    }

    public boolean isAnimateLinkedEnabled() {
        return animateLinkedEnabled;
    }

    public double getAnimateLinkedMaxDistance() {
        return animateLinkedMaxDistance;
    }

    public void animateLink(Player player, Player ally, boolean addAlly) {
        double distanceSquared = player.getLocation().distanceSquared(ally.getLocation());
        if (distanceSquared > animateLinkedMaxDistance * animateLinkedMaxDistance) {
            return;
        }

        final Particle.DustOptions dustOptions = new Particle.DustOptions(addAlly ?
            Color.fromRGB(66, 135, 245) :
            Color.fromRGB(143, 10, 25), 1);



        player.getScheduler().runAtFixedRate(Finale.getPlugin(), task -> {
            Location loc = null;

            Vector dir = ally.getEyeLocation().clone().subtract(player.getEyeLocation()).toVector().normalize();

            if (loc == null) {
                loc = player.getEyeLocation().clone().add(dir);

                loc.getWorld().spawnParticle(Particle.DUST, loc, 1, dustOptions);
            } else {
                Location prevLoc = loc;
                Location newLoc = loc.clone().add(dir);

					/*double particles = 10;
					Vector subDir = newLoc.clone().subtract(prevLoc).toVector();
					double length = subDir.length();
					double ratio = length / particles;
					Vector newDir = subDir.normalize().multiply(ratio);
					Location subLoc = prevLoc.clone();

					for (int i = 0; i < particles; i++) {
						subLoc.add(newDir);
						subLoc.getWorld().spawnParticle(Particle.REDSTONE, subLoc, 1, dustOptions);
					}*/
                ParticleUtil.line(prevLoc, newLoc, (tempLoc) -> {
                    tempLoc.getWorld().spawnParticle(Particle.DUST, tempLoc, 1, dustOptions);
                    return false;
                }, 10);

                loc = newLoc;
            }

            Location dest = ally.getEyeLocation();
            if (!loc.getWorld().getUID().equals(dest.getWorld())) {
                task.cancel();
                return;
            }
            if (loc.distanceSquared(dest) < (1 * 1)) {
                task.cancel();
            }
        }, null, 1L, 1L);
    }

    public void addAlly(Player player, Player ally) {
        Set<UUID> allies = playerAllies.get(player.getUniqueId());
        if (allies == null) {
            allies = new HashSet<>();
        }
        if (allies.contains(ally.getUniqueId())) {
            return;
        }
        allies.add(ally.getUniqueId());
        playerAllies.put(player.getUniqueId(), allies);
        animateLink(player, ally, true);

        Team allyTeam = getAllyTeam(player);
        allyTeam.addEntry(ally.getName());
    }

    public void removeAlly(Player player, Player ally) {
        Set<UUID> allies = playerAllies.get(player.getUniqueId());
        if (allies == null || allies.isEmpty()) {
            return;
        }
        if (!allies.contains(ally.getUniqueId())) {
            return;
        }
        allies.remove(ally.getUniqueId());
        playerAllies.put(player.getUniqueId(), allies);
        animateLink(player, ally, false);

        Team allyTeam = getAllyTeam(player);
        allyTeam.removeEntry(ally.getName());
    }

    private Team getAllyTeam(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        String allyTeamName = player.getUniqueId() + "-ally";
        Team allyTeam = scoreboard.getTeam(allyTeamName);
        if (allyTeam == null) {
            allyTeam = scoreboard.registerNewTeam(allyTeamName);
            allyTeam.setCanSeeFriendlyInvisibles(isSeeInvisAlly());
            allyTeam.setColor(ChatColor.BLUE);
        }
        return allyTeam;
    }

    public Set<UUID> getAllies(Player player) {
        Set<UUID> allies = playerAllies.get(player.getUniqueId());
        if (allies == null) {
            allies = new HashSet<>();

            try {
                PreparedStatement ps = connection.prepareStatement("SELECT AllyUUID FROM ALLY WHERE PlayerUUID = ?");
                ps.setString(1, player.getUniqueId().toString());

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String allyUUIDStr = rs.getString("AllyUUID");
                    UUID allyUUID = UUID.fromString(allyUUIDStr);
                    allies.add(allyUUID);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            playerAllies.put(player.getUniqueId(), allies);
        }
        return allies;
    }

    public boolean isAllyOf(Player attacker, Player target) {
        if (!enabled) {
            return false;
        }

        Set<UUID> allies = getAllies(attacker);
        return allies.contains(target.getUniqueId());
    }

}
