package vg.civcraft.mc.civmodcore.async;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

/// This is intended to reduce friction in writing Folia compatible code. It does add an amount of overhead to PaperMC
/// code. Each type of thread will simply execute on the main thread if on PaperMC, or the appropriate scheduler if on
/// Folia.
public sealed abstract class TaskThread {
    protected final JavaPlugin plugin;

    protected TaskThread(
        final @NotNull JavaPlugin plugin
    ) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    /// This will execute in-place (immediately) if possible, or will schedule the task.
    public final void execute(
        final @NotNull Runnable task
    ) {
        Objects.requireNonNull(task);
        if (canExecuteInPlace()) {
            task.run();
            return;
        }
        switch (PaperRuntime.DETECTED_RUNTIME) {
            case PAPER -> executeOnPaper(task);
            case FOLIA -> executeOnFolia(task);
        }
    }

    /// This is when the task MUST be executed in-place (immediately) or will otherwise throw.
    public final void executeInPlace(
        final @NotNull Runnable task
    ) {
        if (!canExecuteInPlace()) {
            throw new IllegalStateException("Could not execute in place!");
        }
        task.run();
    }

    /// Checks whether the current thread is the intended thread, thus any tasks can be immediately invoked instead of
    /// scheduled.
    protected abstract boolean canExecuteInPlace();

    /// Schedules the task to run on PaperMC's main thread.
    protected void executeOnPaper(
        final @NotNull Runnable task
    ) {
        Bukkit.getScheduler().getMainThreadExecutor(this.plugin).execute(task);
    }

    protected abstract void executeOnFolia(
        final @NotNull Runnable task
    );

    /// Run a task on this task thread, awaiting its completion.
    public void awaitRun(
        final @NotNull Runnable runnable
    ) {
        CompletableFuture.runAsync(runnable, this::execute).join();
    }

    /// Run a supplying task on this task thread, awaiting the result.
    public <T> T awaitGet(
        final @NotNull Supplier<T> getter
    ) {
        return CompletableFuture.supplyAsync(getter, this::execute).join();
    }

    /// Run a consuming task on this task thread, awaiting its completion.
    /// @apiNote If you don't care about capturing-lambdas, feel free to use [#awaitRun(Runnable)] instead.
    public <T> void awaitAccept(
        final T value,
        final @NotNull Consumer<T> consumer
    ) {
        Objects.requireNonNull(consumer);
        CompletableFuture.completedFuture(value).thenAcceptAsync(consumer, this::execute).join();
    }

    /// Run a function on this task thread, awaiting the result.
    /// @apiNote If you don't care about capturing-lambdas, feel free to use [#awaitGet(Supplier)] instead.
    public <T, R> R awaitApply(
        final T value,
        final @NotNull Function<T, R> function
    ) {
        Objects.requireNonNull(function);
        return CompletableFuture.completedFuture(value).thenApplyAsync(function, this::execute).join();
    }

    public static @NotNull TaskThread globalRegion(
        final @NotNull JavaPlugin plugin
    ) {
        return new GlobalRegion(plugin);
    }
    public static final class GlobalRegion extends TaskThread {
        private GlobalRegion(
            final @NotNull JavaPlugin plugin
        ) {
            super(plugin);
        }

        @Override
        protected boolean canExecuteInPlace() {
            return Bukkit.isGlobalTickThread();
        }

        @Override
        protected void executeOnFolia(
            final @NotNull Runnable task
        ) {
            Bukkit.getGlobalRegionScheduler().execute(this.plugin, task);
        }
    }

    public static @NotNull TaskThread region(
        final @NotNull JavaPlugin plugin,
        final @NotNull Location location
    ) {
        return region(
            plugin,
            location.getWorld(),
            WorldUtils.blockToChunkPos(location.getBlockX()),
            WorldUtils.blockToChunkPos(location.getBlockZ())
        );
    }
    public static @NotNull TaskThread region(
        final @NotNull JavaPlugin plugin,
        final @NotNull Block block
    ) {
        return region(
            plugin,
            block.getWorld(),
            WorldUtils.blockToChunkPos(block.getX()),
            WorldUtils.blockToChunkPos(block.getZ())
        );
    }
    public static @NotNull TaskThread region(
        final @NotNull JavaPlugin plugin,
        final @NotNull Chunk chunk
    ) {
        return region(
            plugin,
            chunk.getWorld(),
            chunk.getX(),
            chunk.getZ()
        );
    }
    public static @NotNull TaskThread region(
        final @NotNull JavaPlugin plugin,
        final @NotNull World world,
        final int chunkX,
        final int chunkZ
    ) {
        return new Region(
            plugin,
            world,
            chunkX,
            chunkZ
        );
    }
    public static final class Region extends TaskThread {
        private final World world;
        private final int chunkX;
        private final int chunkZ;

        public Region(
            final @NotNull JavaPlugin plugin,
            final @NotNull World world,
            final int chunkX,
            final int chunkZ
        ) {
            super(plugin);
            this.world = Objects.requireNonNull(world);
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        protected boolean canExecuteInPlace() {
            return Bukkit.isOwnedByCurrentRegion(this.world, this.chunkX, this.chunkZ);
        }

        @Override
        protected void executeOnFolia(
            final @NotNull Runnable task
        ) {
            Bukkit.getRegionScheduler().execute(this.plugin, this.world, this.chunkX, this.chunkZ, task);
        }
    }

    public static @NotNull TaskThread entity(
        final @NotNull JavaPlugin plugin,
        final @NotNull org.bukkit.entity.Entity entity
    ) {
        return new Entity(
            plugin,
            entity
        );
    }
    public static final class Entity extends TaskThread {
        private final org.bukkit.entity.Entity entity;

        public Entity(
            final @NotNull JavaPlugin plugin,
            final @NotNull org.bukkit.entity.Entity entity
        ) {
            super(plugin);
            this.entity = Objects.requireNonNull(entity);
        }

        @Override
        protected boolean canExecuteInPlace() {
            return Bukkit.isOwnedByCurrentRegion(this.entity);
        }

        @Override
        protected void executeOnFolia(
            final @NotNull Runnable task
        ) {
            this.entity.getScheduler().execute(this.plugin, task, null, 0L);
        }
    }
}
