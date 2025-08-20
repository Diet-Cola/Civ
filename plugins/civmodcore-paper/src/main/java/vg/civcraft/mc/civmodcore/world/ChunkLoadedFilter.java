package vg.civcraft.mc.civmodcore.world;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Utility to use with {@link java.util.stream.Stream} to efficiently remove elements from unloaded chunks.
 */
public final class ChunkLoadedFilter {
    /**
     * Creates a new filter function for a given world to remove elements representing blocks in unloaded chunks.
     *
     * @param world The world to filter.
     * @return Returns a new filter function.
     */
    public static Predicate<BlockPos> blockPosition(final World world) {
        Preconditions.checkArgument(WorldUtils.isWorldLoaded(world));
        final List<Long> loadedChunks = new ArrayList<>();
        return (position) -> {
            if (position == null) {
                return false;
            }
            final int chunkX = WorldUtils.blockToChunkPos(position.getX());
            final int chunkZ = WorldUtils.blockToChunkPos(position.getZ());
            final long chunkKey = Chunk.getChunkKey(chunkX, chunkZ);
            if (loadedChunks.contains(chunkKey)) {
                return true;
            }
            if (world.isChunkLoaded(chunkX, chunkZ)) {
                loadedChunks.add(chunkKey);
                return true;
            }
            return false;
        };
    }

    /**
     * Creates a new filter function for a given world to remove elements representing blocks in unloaded chunks.
     *
     * @param world The world to filter.
     * @return Returns a new filter function.
     */
    public static Predicate<Location> location(final World world) {
        Preconditions.checkArgument(WorldUtils.isWorldLoaded(world));
        final List<Long> loadedChunks = new ArrayList<>();
        return (position) -> {
            if (position == null) {
                return false;
            }
            final int chunkX = WorldUtils.blockToChunkPos(position.getBlockX());
            final int chunkZ = WorldUtils.blockToChunkPos(position.getBlockZ());
            final long chunkKey = Chunk.getChunkKey(chunkX, chunkZ);
            if (loadedChunks.contains(chunkKey)) {
                return true;
            }
            if (world.isChunkLoaded(chunkX, chunkZ)) {
                loadedChunks.add(chunkKey);
                return true;
            }
            return false;
        };
    }
}
