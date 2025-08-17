package vg.civcraft.mc.civmodcore.async;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/// Intended to make working with [java.util.concurrent.CompletableFuture] and other such APIs easier, since Folia's
/// schedulers do not implement [java.util.concurrent.Executor].
///
/// ```
/// final var executors = new CivFoliaExecutors(ExamplePlugin.getInstance());
/// CompletableFuture.runAsync(() -> { /** DO SOMETHING */ }, executors.global());
/// ```
///
/// @apiNote Please only use each executor how their respective scheduler defines. See each method's respective
///          "see"/"see also" for more information.
public final class CivFoliaExecutors {
    private final JavaPlugin plugin;

    public CivFoliaExecutors(
        final @NotNull JavaPlugin plugin
    ) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public CivFoliaExecutors(
        final @NotNull Class<? extends JavaPlugin> pluginClass
    ) {
        this(JavaPlugin.getPlugin(pluginClass));
    }

    /**
     * @see Bukkit#getAsyncScheduler()
     */
    public @NotNull Executor async() {
        return (task) -> Bukkit.getAsyncScheduler().runNow(this.plugin, (ctx) -> task.run());
    }

    /**
     * @see Bukkit#getGlobalRegionScheduler()
     */
    public @NotNull Executor global() {
        return (task) -> Bukkit.getGlobalRegionScheduler().execute(this.plugin, task);
    }

    /**
     * @param location This must be a BLOCK location.
     * @see Bukkit#getRegionScheduler()
     */
    public @NotNull Executor region(
        final @NotNull Location location
    ) {
        return region(
            location.getWorld(),
            location.getBlockX() << 4,
            location.getBlockZ() << 4
        );
    }

    /**
     * @see Bukkit#getRegionScheduler()
     */
    public @NotNull Executor region(
        final @NotNull World world,
        final int chunkX,
        final int chunkZ
    ) {
        Objects.requireNonNull(world);
        return (task) -> Bukkit.getRegionScheduler().execute(this.plugin, world, chunkX, chunkZ, task);
    }

    /// Use this in conjunction with [CompletableFuture#thenCompose(Function)], eg:
    /// ```
    /// final var executors = new CivFoliaExecutors(ExamplePlugin.getInstance());
    /// CompletableFuture.supplyAsync(() -> Bukkit.getPlayer("Orinnari"), executors.global())
    ///     .thenCompose(executors.entity((player) -> {
    ///         player.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
    ///     }));
    /// ```
    /// Will return the entity given to it.
    ///
    /// @see Entity#getScheduler()
    /// @apiNote This could be far less obnoxious if Java had extension methods D:
    public <E extends Entity> @NotNull Function<@NotNull E, @NotNull CompletableFuture<@NotNull E>> entity(
        final @NotNull Consumer<@NotNull E> task
    ) {
        Objects.requireNonNull(task);
        return (entity) -> {
            final var future = new CompletableFuture<E>();
            if (entity == null) {
                future.completeExceptionally(new NullPointerException("Provided entity cannot be null!"));
            }
            else {
                entity.getScheduler().execute(this.plugin, () -> {
                    try {
                        task.accept(entity);
                    }
                    catch (final Exception e) {
                        future.completeExceptionally(e);
                        return;
                    }
                    future.complete(entity);
                }, null, 0L);
            }
            return future;
        };
    }

    /// Use this in conjunction with [CompletableFuture#thenCompose(Function)], eg:
    /// ```
    /// final var executors = new CivFoliaExecutors(ExamplePlugin.getInstance());
    /// CompletableFuture.supplyAsync(() -> Bukkit.getPlayer("Orinnari"), executors.global())
    ///     .thenCompose(executors.entitySupply((player) -> {
    ///         final var loot = new ItemStack(Material.DIAMOND, 64);
    ///         player.getInventory().addItem(loot);
    ///         return loot;
    ///     }));
    /// ```
    /// @see Entity#getScheduler()
    /// @apiNote This could be far less obnoxious if Java had extension methods D:
    public <R, E extends Entity> @NotNull Function<@NotNull E, @NotNull CompletableFuture<R>> entitySupply(
        final @NotNull Function<@NotNull E, R> task
    ) {
        Objects.requireNonNull(task);
        return (entity) -> {
            final var future = new CompletableFuture<R>();
            if (entity == null) {
                future.completeExceptionally(new NullPointerException("Provided entity cannot be null!"));
            }
            else {
                entity.getScheduler().execute(this.plugin, () -> {
                    final R result;
                    try {
                        result = task.apply(entity);
                    }
                    catch (final Exception e) {
                        future.completeExceptionally(e);
                        return;
                    }
                    future.complete(result);
                }, null, 0L);
            }
            return future;
        };
    }

    /// Use this in conjunction with [CompletableFuture#thenCompose(Function)], eg:
    /// ```
    /// final var executors = new CivFoliaExecutors(ExamplePlugin.getInstance());
    /// CompletableFuture.supplyAsync(Bukkit::getOnlinePlayers, executors.global())
    ///     .thenCompose(executors.entities((player) -> {
    ///         player.getInventory().addItem(new ItemStack(Material.STICK, 1));
    ///     }));
    /// ```
    /// Will return the entity collection given to it.
    ///
    /// @see Entity#getScheduler()
    /// @apiNote This could be far less obnoxious if Java had extension methods D:
//    public <E extends Entity, C extends Collection<E>> @NotNull Function<@NotNull C, @NotNull CompletableFuture<@NotNull C>> entities(
//        final @NotNull Consumer<@NotNull E> task
//    ) {
//        Objects.requireNonNull(task);
//        return (entities) -> CompletableFuture.allOf(
//            entities.stream()
//                .map(entity(task))
//                .toArray(CompletableFuture[]::new)
//        ).thenApply((result) -> entities);
//    }
}
