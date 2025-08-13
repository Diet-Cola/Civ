package net.minelink.ctplus.task;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.Tag;
import net.minelink.ctplus.util.BarUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class TagUpdateTask extends BukkitRunnable {

    private final static Map<UUID, ScheduledTask> tasks = new HashMap<>();

    private final CombatTagPlus plugin;

    private final UUID playerId;

    private TagUpdateTask(CombatTagPlus plugin, Player player) {
        this.plugin = plugin;
        this.playerId = player.getUniqueId();
    }

    @Override
    public void run() {
        // Cancel if player went offline
        Player player = plugin.getPlayerCache().getPlayer(playerId);
        if (player == null) {
            tasks.get(playerId).cancel();
            return;
        }

        // Remove bar before displaying the next one
        if (plugin.getSettings().useBarApi() && BarUtils.hasBar(player)) {
            BarUtils.removeBar(player);
        }

        // Cancel if player is no longer tagged
        Tag tag = plugin.getTagManager().getTag(playerId);
        if (tag == null || tag.isExpired()) {
            if (plugin.getSettings().useBarApi()) {
                BarUtils.setMessage(player, plugin.getSettings().getBarApiEndedMessage(), 1);
            }

            if (!plugin.getSettings().getUntagMessage().isEmpty()) {
                player.sendMessage(plugin.getSettings().getUntagMessage());
            }
            tasks.get(playerId).cancel();
            return;
        }

        if (plugin.getSettings().useBarApi()) {
            int remainingDuration = tag.getTagDuration();
            int tagDuration = plugin.getSettings().getTagDuration();
            float percent = ((float) remainingDuration / tagDuration) * 100;
            String remaining = plugin.getSettings().formatDuration(remainingDuration);

            // Display remaining timer in boss bar
            String message = plugin.getSettings().getBarApiCountdownMessage().replace("{remaining}", remaining);
            BarUtils.setMessage(player, message, percent);
        }
    }

    public static void run(final CombatTagPlus plugin, final Player p) {
        // Do nothing if player is a NPC
        if (plugin.getNpcPlayerHelper().isNpc(p)) return;

        final RegionScheduler s = Bukkit.getRegionScheduler();

        // Schedule the task to run on next tick
        s.run(plugin,  p.getLocation(), task ->  {
                // Do nothing if player isn't tagged or online
                if (!plugin.getTagManager().isTagged(p.getUniqueId()) || !p.isOnline()) {
                    return;
                }

                UUID playerId = p.getUniqueId();
                ScheduledTask playerTask = tasks.get(playerId);

                // Do nothing if player already has an active task
                if (playerTask != null) {
                    return;
                }

                // Create new repeating task

                playerTask = p.getScheduler().runAtFixedRate(plugin, newTask -> new TagUpdateTask(plugin, p).run(), null, 1L, 5L);
                tasks.put(playerId, playerTask);
        });
    }

    public static void purgeFinished() {
        Iterator<ScheduledTask> iterator = tasks.values().iterator();

        // Loop over each task
        while (iterator.hasNext()) {
            ScheduledTask taskId = iterator.next();

            // Remove entry if task isn't running anymore
            if (taskId.isCancelled()) {
                iterator.remove();
            }
        }
    }

    public static void cancelTasks(CombatTagPlus plugin) {
        Iterator<UUID> iterator = tasks.keySet().iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Player player = plugin.getPlayerCache().getPlayer(uuid);
            if (player != null && plugin.getSettings().useBarApi() && BarUtils.hasBar(player)) {
                BarUtils.removeBar(player);
            }

            ScheduledTask taskId = tasks.get(uuid);

            if (!taskId.isCancelled()) {
                taskId.cancel();
            }

            iterator.remove();
        }
    }

}
