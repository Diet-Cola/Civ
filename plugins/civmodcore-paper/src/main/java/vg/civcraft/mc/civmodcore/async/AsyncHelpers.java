package vg.civcraft.mc.civmodcore.async;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import vg.civcraft.mc.civmodcore.ACivMod;

public final class AsyncHelpers {
    public static void logCurrentThread(
        final @NotNull Logger logger,
        final Object owner
    ) {
        logger.info(
            "Thread[{}] [primary:{}] [global:{}] [{}]",
            Thread.currentThread().getName(),
            Bukkit.isPrimaryThread(),
            Bukkit.isGlobalTickThread(),
            switch (owner) {
                case final Entity entity -> "owned:entity:" + Bukkit.isOwnedByCurrentRegion(entity);
                case final Location location -> "owned:region:" + Bukkit.isOwnedByCurrentRegion(location);
                case final Block block -> "owned:region:" + Bukkit.isOwnedByCurrentRegion(block);
                case final Chunk chunk -> "owned:region:" + Bukkit.isOwnedByCurrentRegion(chunk.getWorld(), chunk.getX(), chunk.getZ());
                case null, default -> "owned:false";
            }
        );
    }

    public static final long TICK_MS = 1000 / 20;

    public static void assertSyncThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("This must be called from the server thread.");
        }
    }

    public static void assertVirtualThread() {
        if (!Thread.currentThread().isVirtual()) {
            throw new IllegalStateException("This must be called from a virtual thread.");
        }
    }

    public static void runOnNextTick(
        final @NotNull ACivMod plugin,
        final @NotNull Runnable task
    ) {
        Objects.requireNonNull(plugin);
        Objects.requireNonNull(task);
        switch (PaperRuntime.DETECTED_RUNTIME) {
            case PAPER -> Bukkit.getScheduler().runTask(plugin, task);
            case FOLIA -> VirtualExecutor.get(plugin).execute(() -> {
                if (AsyncHelpers.awokenBefore(AsyncHelpers.TICK_MS, TimeUnit.MILLISECONDS)) return;
                task.run();
            });
        }
    }

    /// Puts the thread to sleep, eg:
    /// ```
    /// if (AsyncHelpers.awokenBefore(1, TimeUnit.SECONDS)) return;
    /// ```
    /// @return Returns true if the sleep was interrupted. Use this to exit gracefully.
    public static @CheckReturnValue boolean awokenBefore(
        final long delay,
        final @NotNull TimeUnit unit
    ) {
        try {
            Thread.sleep(unit.toMillis(delay));
            return false;
        }
        catch (final InterruptedException ignored) {
            return true;
        }
    }
}
