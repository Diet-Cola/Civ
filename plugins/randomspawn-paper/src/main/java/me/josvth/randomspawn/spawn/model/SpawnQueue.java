package me.josvth.randomspawn.spawn.model;

import java.util.concurrent.ArrayBlockingQueue;
import me.josvth.randomspawn.RandomSpawn;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public record SpawnQueue(
    @NotNull ArrayBlockingQueue<@NotNull Location> queue
) {
    private static final int CAPACITY = 3;

    public SpawnQueue() {
        this(new ArrayBlockingQueue<>(CAPACITY));
    }

    public boolean hasCapacity() {
        return queue().remainingCapacity() > 0;
    }

    public void add(
        final @NotNull RandomSpawn plugin,
        final @NotNull Location spawn
    ) {
        spawn.getWorld().addPluginChunkTicket(
            WorldUtils.blockToChunkPos(spawn.getBlockX()),
            WorldUtils.blockToChunkPos(spawn.getBlockZ()),
            plugin
        );
        queue().offer(spawn);
    }

    public @Nullable Location poll(
        final @NotNull RandomSpawn plugin
    ) {
        final Location polled = queue().poll();
        if (polled != null) {
            polled.getWorld().removePluginChunkTicket(
                WorldUtils.blockToChunkPos(polled.getBlockX()),
                WorldUtils.blockToChunkPos(polled.getBlockZ()),
                plugin
            );
        }
        return polled;
    }
}
