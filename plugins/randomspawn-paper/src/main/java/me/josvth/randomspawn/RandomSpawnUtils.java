package me.josvth.randomspawn;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RandomSpawnUtils {
	public static final String LAST_TIME_RANDOM_SPAWNED_KEY = "lasttimerandomspawned";

	public static @Nullable Long getLastTimeRandomSpawned(
		final @NotNull Player player
	) {
		Long timestamp = null;
		for (final MetadataValue meta : player.getMetadata(LAST_TIME_RANDOM_SPAWNED_KEY)) {
			final long currentTimestamp = meta.asLong();
			if (timestamp == null || currentTimestamp > timestamp) {
				timestamp = currentTimestamp;
			}
		}
		return timestamp;
	}

	public static void setLastTimeRandomSpawned(
		final @NotNull RandomSpawn plugin,
		final @NotNull Player player,
		final long timestamp
	) {
		player.setMetadata(
			LAST_TIME_RANDOM_SPAWNED_KEY,
			new FixedMetadataValue(plugin, timestamp)
		);
	}

    public static boolean hasLastTimeRandomSpawned(
        final @NotNull Player player
    ) {
        return player.hasMetadata(LAST_TIME_RANDOM_SPAWNED_KEY);
    }
}
