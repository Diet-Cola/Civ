package me.josvth.randomspawn.spawn;

import isaac.bastion.Bastion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.worlds.CitySpawn;
import me.josvth.randomspawn.config.worlds.RandomSpawnArea;
import me.josvth.randomspawn.config.worlds.SpawnByPlayerArea;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import me.josvth.randomspawn.spawn.model.BlockXZ;
import org.apache.commons.lang3.IntegerRange;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import vg.civcraft.mc.civmodcore.async.AsyncHelpers;
import vg.civcraft.mc.civmodcore.async.PaperRuntime;
import vg.civcraft.mc.civmodcore.async.TaskThread;
import vg.civcraft.mc.civmodcore.utilities.MoreCollectionUtils;
import vg.civcraft.mc.civmodcore.utilities.Unreachable;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class SpawnSelector {
    protected final RandomSpawn plugin;

    public SpawnSelector(
        final @NotNull RandomSpawn plugin
    ) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    /// Finds all the valid spawn points from the full set of configured spawn points in the world. What is valid?
    /// Spawn points can configurably require another player to be nearby, or allow spawn there regardless of nearby
    /// players. This function checks that, and checks it against the online players. If a player is sufficiently near
    /// as determined by the "checkradius", or if not set, the "radius" of the spawn point, then use that point. Of
    /// course if a nearby player is not required, the spawn point is added.
    ///
    /// The final result of these checks is returned as the set of eligible spawn points; from which one will ultimately
    /// be chosen and used.
    ///
    /// @param world The world to restrict the check to. Only players from that world are considered.
    /// @return A location near a valid spawn point.
    ///
    /// @author ProgrammerDan programmerdan@gmail.com
    public @Nullable Location getSpawnPoint(
        final @NotNull World world
    ) {
        switch (PaperRuntime.DETECTED_RUNTIME) {
            case PAPER -> AsyncHelpers.assertSyncThread();
            case FOLIA -> AsyncHelpers.assertVirtualThread();
        }
        final WorldConfig worldConfig = this.plugin.configs.worlds.get(world.getName());
        if (worldConfig == null || worldConfig.cities().isEmpty()) {
            return null;
        }
        final List<CitySpawn> cities = new ArrayList<>(worldConfig.cities().values());
        Collections.shuffle(cities, ThreadLocalRandom.current());
        for (final CitySpawn city : cities) {
            if (city.requiredNearbyPlayerRadius() instanceof final Integer requiredNearbyPlayerRadius) {
                if (!hasNearbyPlayers(world, city.x(), city.z(), requiredNearbyPlayerRadius)) {
                    continue; // Found no-one near this city, try next city
                }
            }
            final Location respawnLocation = chooseSpawn(
                world,
                worldConfig,
                chooseCircleSpawnXZ(
                    city.radius(),
                    city.exclusionRadius(),
                    city.x(),
                    city.z()
                )
            );
            if (respawnLocation == null) {
                continue; // Could not generate a spawn location, try next city
            }
            return respawnLocation;
        }
        return null;
    }

    /// Random spawning will first try to place you near another player, if configured to do so, or otherwise will put
    /// you anywhere within a configured area.
    public @Nullable Location getRandomSpawn(
        final @NotNull World world
    ) {
        switch (PaperRuntime.DETECTED_RUNTIME) {
            case PAPER -> AsyncHelpers.assertSyncThread();
            case FOLIA -> AsyncHelpers.assertVirtualThread();
        }
        final WorldConfig worldConfig = this.plugin.configs.worlds.get(world.getName());
        if (worldConfig == null) {
            return null;
        }
        if (worldConfig.spawnByPlayer() instanceof SpawnByPlayerArea(int radius, int exclusionRadius)) {
            final List<Player> players = new ArrayList<>(world.getPlayers());
            players.removeIf(Player::isDead);
            if (!players.isEmpty()) {
                final Player anchor = MoreCollectionUtils.randomElement(players, ThreadLocalRandom.current());
                assert anchor != null;
                for (int i = 0; i < 1000; i++) {
                    final Location respawnLocation = chooseSpawn(
                        world,
                        worldConfig,
                        chooseCircleSpawnXZ(
                            radius,
                            exclusionRadius,
                            (int) anchor.getX(),
                            (int) anchor.getZ()
                        )
                    );
                    if (respawnLocation == null) {
                        continue;
                    }
                    return respawnLocation;
                }
            }
        }
        for (int i = 0; i < 1000; i++) {
            final Location respawnLocation = chooseSpawn(
                world,
                worldConfig,
                chooseRandomSpawnXZ(worldConfig.randomSpawnArea())
            );
            if (respawnLocation == null) {
                continue; // retry
            }
            if (Bukkit.getServer().getPluginManager().isPluginEnabled("Bastion")) {
                if (!Bastion.getBastionManager().getBlockingBastions(respawnLocation).isEmpty()) {
                    continue;
                }
            }
            return respawnLocation;
        }
        return null;
    }

    private @Nullable Location chooseSpawn(
        final @NotNull World world,
        final @NotNull WorldConfig worldConfig,
        final @NotNull BlockXZ blockXZ
    ) {
        final int chunkX = WorldUtils.blockToChunkPos(blockXZ.blockX());
        final int chunkZ = WorldUtils.blockToChunkPos(blockXZ.blockZ());
        final Chunk chunk = switch (PaperRuntime.DETECTED_RUNTIME) {
            case PAPER -> {
                AsyncHelpers.assertSyncThread();
                yield world.getChunkAt(chunkX, chunkZ);
            }
            case FOLIA -> {
                AsyncHelpers.assertVirtualThread();
                yield TaskThread.region(this.plugin, world, chunkX, chunkZ)
                    .awaitGet(() -> world.getChunkAtAsync(chunkX, chunkZ, true))
                    .join();
            }
        };
        // TODO: Remove the awaitRun if accessing this information from other threads is fine
        final var specs = TaskThread.globalRegion(this.plugin).awaitGet(() -> {
            record WorldSpecs(int minHeight, int maxHeight, boolean scanFromTop) {}
            if (world.getEnvironment() == World.Environment.NETHER) {
                // TODO: Vanilla nether has some *weird* behaviour: The roof-bedrock will be at y128, but the actual
                //       world-height is 256, so using the max-height API would result in players occasionally getting
                //       spawned above the roof. The empty blocks above the roof are AIR too, not VOID_AIR, so you
                //       cannot check for that either.
                return new WorldSpecs(
                    0, // world.getMinHeight(),
                    128, // world.getMaxHeight() - 1,
                    false
                );
            }
            else {
                // TODO: This is here to prevent players from rare instances of being spawned deep
                //       underground. Lava lakes typically only generate at around y30, so players
                //       unfortunate enough to be spawned underground should have relative safety.
                //       Keep in mind that this also applies to other dimensions like the End.
                return new WorldSpecs(
                    40, // world.getMinHeight(),
                    world.getMaxHeight() - 1,
                    true
                );
            }
        });
        return TaskThread.region(this.plugin, chunk).awaitGet(() -> WorldUtils.findValidSpawnInColumn(
            chunk,
            WorldUtils.blockToInternalChunkPos(blockXZ.blockX()),
            WorldUtils.blockToInternalChunkPos(blockXZ.blockZ()),
            IntegerRange.of(specs.minHeight(), specs.maxHeight()),
            specs.scanFromTop(),
            Set.copyOf(worldConfig.blacklist())
        ));
    }

    /// Convenience shortcut
    /// @apiNote This just chooses a random block X and Z regardless of what type of random spawn area you give it.
    private static @NotNull BlockXZ chooseRandomSpawnXZ(
        final @NotNull RandomSpawnArea randomSpawnArea
    ) {
        return switch (randomSpawnArea) {
            case final RandomSpawnArea.Circle circle -> chooseCircleSpawnXZ(
                circle.radius(),
                circle.exclusionRadius(),
                circle.centreX(),
                circle.centreZ()
            );
            case final RandomSpawnArea.Square square -> chooseSquareSpawnXZ(
                square.minX(),
                square.maxX(),
                square.minZ(),
                square.maxZ(),
                square.thickness()
            );
        };
    }

    /// Generates a single block X,Z location within a given circle.
    private static @NotNull BlockXZ chooseCircleSpawnXZ(
        final double radius,
        final double exclusionRadius,
        final int centreX,
        final int centreZ
    ) {
        // Uniformly distributed in "annulus". Explanation: https://forum.unity.com/threads/random-point-within-circle-with-min-max-radius.597523/#post-8524934
        final double ex2 = exclusionRadius * exclusionRadius;
        final double r2 = radius * radius;

        final double r = Math.sqrt(Math.random() * (r2 - ex2) + ex2);
        final double phi = Math.random() * 2d * Math.PI;
        final double x = Math.round(centreX + Math.cos(phi) * r);
        final double z = Math.round(centreZ + Math.sin(phi) * r);

        return new BlockXZ((int) x, (int) z);
    }

    /// Generates a single block X,Z location within a given AABB range.
    private static @NotNull BlockXZ chooseSquareSpawnXZ(
        final double minX,
        final double maxX,
        final double minZ,
        final double maxZ,
        final double thickness // uwu
    ) {
        final Random random = ThreadLocalRandom.current();
        if (thickness > 0) {
            final double borderOffset = random.nextDouble() * thickness;
            return switch ((int) random.nextDouble(0, 4)) {
                case 0 -> new BlockXZ(
                    minX + borderOffset,
                    // Also balancing probability considering thickness
                    minZ + random.nextDouble() * (maxZ - minZ + 1 - 2 * thickness) + thickness
                );
                case 1 -> new BlockXZ(
                    maxX - borderOffset,
                    // Also balancing probability considering thickness
                    minZ + random.nextDouble() * (maxZ - minZ + 1 - 2 * thickness) + thickness
                );
                case 2 -> new BlockXZ(
                    minX + random.nextDouble() * (maxX - minX + 1),
                    // Also balancing probability considering thickness
                    minZ + borderOffset
                );
                case 3 -> new BlockXZ(
                    minX + random.nextDouble() * (maxX - minX + 1),
                    // Also balancing probability considering thickness
                    maxZ - borderOffset
                );
                default -> throw new Unreachable();
            };
        }
        return new BlockXZ(
            minX + random.nextDouble() * (maxX - minX + 1),
            minZ + random.nextDouble() * (maxZ - minZ + 1)
        );
    }

    public boolean hasNearbyPlayers(
        final @NotNull World world,
        final int chunkX,
        final int chunkZ,
        final @Range(from = 0, to = Integer.MAX_VALUE) int range
    ) {
        return switch (PaperRuntime.DETECTED_RUNTIME) {
            case PAPER -> {
                AsyncHelpers.assertSyncThread();
                final var location = new Location(
                    world,
                    WorldUtils.chunkToBlockPos(chunkX, 0),
                    0,
                    WorldUtils.chunkToBlockPos(chunkZ, 0)
                );
                // Creating a new ArrayList just in case.
                final List<Player> nearby = new ArrayList<>(location.getNearbyPlayers(range, Double.MAX_VALUE));
                nearby.removeIf(Player::isDead);
                yield !nearby.isEmpty();
            }
            case FOLIA -> {
                AsyncHelpers.assertVirtualThread();
                final int chunkRangeMinX = chunkX - range;
                final int chunkRangeMaxX = chunkX + range;
                final int chunkRangeMinZ = chunkZ - range;
                final int chunkRangeMaxZ = chunkZ + range;
                // TODO: Test whether this is fine to do from a virtual thread, or whether reading the player location
                //       needs to be done from the player thread. Likewise whether "world.getPlayers()" needs to be
                //       called from the global region scheduler.
                for (final Player player : world.getPlayers()) {
                    if (player.isDead()) {
                        continue;
                    }
                    final int playerChunkX = WorldUtils.blockToChunkPos(player.getX());
                    final int playerChunkZ = WorldUtils.blockToChunkPos(player.getZ());
                    if (playerChunkX >= chunkRangeMinX
                        && playerChunkX <= chunkRangeMaxX
                        && playerChunkZ >= chunkRangeMinZ
                        && playerChunkZ <= chunkRangeMaxZ
                    ) {
                        yield true;
                    }
                }
                yield false;
            }
        };
    }
}
