package vg.civcraft.mc.civmodcore.async;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.utilities.MoreMapUtils;

/// This is so that each plugin can have its own virtual-thread executor, which can then be shutdown by
/// [ACivMod#onDisable()].
public final class VirtualExecutor {
    private static final Map<String, ExecutorService> executors = new ConcurrentHashMap<>();

    public static @NotNull ExecutorService get(
        final @NotNull ACivMod plugin
    ) {
        return MoreMapUtils.putIfAbsent(executors, plugin.getName(), Executors::newVirtualThreadPerTaskExecutor);
    }

    @ApiStatus.Internal
    public static void stop(
        final @NotNull ACivMod plugin
    ) {
        final ExecutorService scheduler = executors.remove(plugin.getName());
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
