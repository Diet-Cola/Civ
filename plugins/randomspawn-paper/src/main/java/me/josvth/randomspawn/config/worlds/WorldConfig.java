package me.josvth.randomspawn.config.worlds;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import java.util.Set;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;
import vg.civcraft.mc.civmodcore.config.ConfigValueType;

/// @param spawnByPlayer If this is null, it's because "spawnbyplayer" was false.
public record WorldConfig(
    @NotNull RespawnFlags randomSpawnOn,
    @NotNull RespawnFlags spawnPointOn,
    boolean keepRandomSpawn,
    @Range(from = 0, to = Long.MAX_VALUE) long newPlayerPeriod,
    @Nullable FirstSpawn firstSpawn,
    @NotNull RandomSpawnArea randomSpawnArea,
    @Nullable SpawnByPlayerArea spawnByPlayer,
    @NotNull Set<@NotNull Material> blacklist,
    @NotNull Map<@NotNull String, @NotNull CitySpawn> cities
) {
    public WorldConfig {
        Objects.requireNonNull(randomSpawnOn);
        Objects.requireNonNull(spawnPointOn);
        blacklist = Set.copyOf(blacklist);
        cities = Map.copyOf(cities);
    }

    public static final String KEY_RANDOM_SPAWN_ON = "randomspawnon";
    public static final String KEY_SPAWN_POINT_ON = "spawnpointson";
    public static final String KEY_KEEP_RANDOM_SPAWN = "keeprandomspawns";
    public static final String KEY_NEW_PLAYER_PERIOD = "newplayertime";
    public static final String KEY_FIRST_SPAWN = "firstspawn";
    public static final String KEY_SPAWN_AREA = "spawnarea";
    public static final String KEY_SPAWN_BY_PLAYER_ENABLED = "spawnbyplayer";
    public static final String KEY_SPAWN_BY_PLAYER_AREA = "spawnbyplayerarea";
    public static final String KEY_SPAWN_BLACKLIST = "spawnblacklist";
    public static final String KEY_CITY_SPAWNS = "spawnpoints";

    public static @NotNull WorldConfig parse(
        final @NotNull ConfigurationSection worldSection
    ) throws ConfigParseException {
        return new WorldConfig(
            RespawnFlags.parse(ConfigHelpers.getOrNull(worldSection, KEY_RANDOM_SPAWN_ON, ConfigValueType.SECTION)),
            RespawnFlags.parse(ConfigHelpers.getOrNull(worldSection, KEY_SPAWN_POINT_ON, ConfigValueType.SECTION)),
            ConfigHelpers.getOrElse(worldSection, KEY_KEEP_RANDOM_SPAWN, ConfigValueType.BOOL, false),
            ConfigHelpers.getOrElse(worldSection, KEY_NEW_PLAYER_PERIOD, ConfigValueType.UINT, 0),
            FirstSpawn.parse(ConfigHelpers.getOrNull(worldSection, KEY_FIRST_SPAWN, ConfigValueType.SECTION)),
            RandomSpawnArea.parse(ConfigHelpers.get(worldSection, KEY_SPAWN_AREA, ConfigValueType.SECTION)),
            ConfigHelpers.getOrElse(worldSection, KEY_SPAWN_BY_PLAYER_ENABLED, ConfigValueType.BOOL, false)
                ? SpawnByPlayerArea.parse(ConfigHelpers.get(worldSection, KEY_SPAWN_BY_PLAYER_AREA, ConfigValueType.SECTION))
                : null,
            parseBlacklist(worldSection),
            CitySpawn.parse(ConfigHelpers.getOrNull(worldSection, KEY_CITY_SPAWNS, ConfigValueType.SECTION))
        );
    }

    /// Add default blacklist if the config is empty
    private static @NotNull Set<@NotNull Material> parseBlacklist(
        final @NotNull ConfigurationSection worldSection
    ) throws ConfigParseException {
        final List<Material> blacklist = ConfigHelpers.getOrNull(worldSection, KEY_SPAWN_BLACKLIST, ConfigValueType.MATERIAL_LIST);
        if (blacklist == null) {
            return Set.of(
                Material.WATER,
                Material.LAVA,
                Material.FIRE,
                Material.CACTUS,
                Material.MAGMA_BLOCK
            );
        }
        return Set.copyOf(blacklist);
    }

    public @NotNull Location getFirstSpawn(
        final @NotNull World world
    ) {
        final FirstSpawn firstSpawn = firstSpawn();
        if (firstSpawn == null) {
            return world.getSpawnLocation();
        }
        return new Location(
            world,
            firstSpawn.x(),
            firstSpawn.y(),
            firstSpawn.z(),
            firstSpawn.yaw(),
            firstSpawn.pitch()
        );
    }
}
