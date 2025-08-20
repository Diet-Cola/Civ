package me.josvth.randomspawn.spawn;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.spawn.model.LocationGenerator;
import me.josvth.randomspawn.spawn.model.SpawnQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.async.AsyncHelpers;
import vg.civcraft.mc.civmodcore.async.PaperRuntime;
import vg.civcraft.mc.civmodcore.async.VirtualExecutor;
import vg.civcraft.mc.civmodcore.utilities.MoreMapUtils;

/**
 * Pre-emptively loads chunks asynchronously via a queue to avoid lagging the server and loading chunks
 * synchronously when a respawn is needed. It will only block if the queue has been entirely consumed.
 */
public class CachedSpawnSelector extends SpawnSelector implements Listener {
    private final ConcurrentMap<String, SpawnQueue> randomSpawns = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SpawnQueue> spawnPoints = new ConcurrentHashMap<>();

    public CachedSpawnSelector(
        final @NotNull RandomSpawn plugin
    ) {
        super(plugin);
        plugin.registerListener(this);
        switch (PaperRuntime.DETECTED_RUNTIME) {
            case PAPER -> Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::cycleLocations,
                20 * 40, // 40 seconds
                20 * 40 // 40 seconds
            );
            case FOLIA -> VirtualExecutor.get(plugin).execute(() -> {
                while (true) {
                    if (AsyncHelpers.awokenBefore(40, TimeUnit.SECONDS)) {
                        return;
                    }
                    cycleLocations();
                }
            });
        }
        for (final World world : Bukkit.getWorlds()) {
            on(new WorldLoadEvent(world));
        }
    }

    private void cycleLocations() {
        this.spawnPoints.forEach((worldName, spawns) -> queue(worldName, spawns, super::getSpawnPoint));
        this.randomSpawns.forEach((worldName, spawns) -> queue(worldName, spawns, super::getRandomSpawn));
    }

    private void queue(
        final @NotNull String worldName,
        final @NotNull SpawnQueue queue,
        final @NotNull LocationGenerator locationGenerator
    ) {
        switch (PaperRuntime.DETECTED_RUNTIME) {
            case PAPER -> AsyncHelpers.assertSyncThread();
            case FOLIA -> AsyncHelpers.assertVirtualThread();
        }
        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }
        final Location generated = locationGenerator.generate(world);
        if (generated == null) {
            AsyncHelpers.runOnNextTick(this.plugin, () -> queue(worldName, queue, locationGenerator));
            return;
        }
        if (!queue.hasCapacity()) {
            queue.poll(this.plugin);
        }
        queue.add(plugin, generated);
        if (queue.hasCapacity()) {
            AsyncHelpers.runOnNextTick(this.plugin, () -> queue(worldName, queue, locationGenerator));
        }
    }

    @EventHandler
    public void on(
        final @NotNull WorldLoadEvent event
    ) {
        final String worldName = event.getWorld().getName();
        if (this.plugin.configs.worlds.containsKey(worldName)) {
            MoreMapUtils.putIfAbsent(this.spawnPoints, worldName, SpawnQueue::new);
            MoreMapUtils.putIfAbsent(this.randomSpawns, worldName, SpawnQueue::new);
            VirtualExecutor.get(this.plugin).execute(this::cycleLocations);
        }
    }

    @EventHandler
    public void on(
        final @NotNull WorldUnloadEvent event
    ) {
        final String worldName = event.getWorld().getName();
        this.spawnPoints.remove(worldName);
        this.randomSpawns.remove(worldName);
    }

    @Override
    public @Nullable Location getSpawnPoint(
        final @NotNull World world
    ) {
        final SpawnQueue spawns = this.spawnPoints.get(world.getName());
        if (spawns == null) {
            return null;
        }
        final Location spawn = spawns.poll(this.plugin);
        if (spawn != null) {
            VirtualExecutor.get(this.plugin).execute(() -> queue(world.getName(), spawns, super::getSpawnPoint));
            return spawn;
        }
        return null;
    }

    @Override
    public @Nullable Location getRandomSpawn(
        final @NotNull World world
    ) {
        final SpawnQueue spawns = this.randomSpawns.get(world.getName());
        if (spawns == null) {
            return null;
        }
        final Location spawn = spawns.poll(this.plugin);
        if (spawn != null) {
            VirtualExecutor.get(this.plugin).execute(() -> queue(world.getName(), spawns, super::getRandomSpawn));
            return spawn;
        }
        return null;
    }
}

