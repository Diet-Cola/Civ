package net.minelink.ctplus.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.Npc;

public class NpcDespawnTask implements Runnable {

    private final CombatTagPlus plugin;

    private final Npc npc;

    private long time;

    private ScheduledTask taskId;

    public NpcDespawnTask(CombatTagPlus plugin, Npc npc, long time) {
        this.plugin = plugin;
        this.npc = npc;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Npc getNpc() {
        return npc;
    }

    public void start() {
        taskId = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> this.run(), 1, 1);
    }

    public void stop() {
        this.taskId.cancel();
    }

    @Override
    public void run() {
        // Do nothing if NPC should not despawn yet
        if (time > System.currentTimeMillis()) {
            return;
        }

        // Despawn the NPC
        plugin.getNpcManager().despawn(npc);
    }

}
