package me.josvth.randomspawn.config.worlds;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;
import vg.civcraft.mc.civmodcore.config.ConfigValueType;

/// @param requiredNearbyPlayerRadius If this value is null then nearby players are not required.
public record CitySpawn(
    int x,
    int y,
    int z,
    @Nullable Integer requiredNearbyPlayerRadius,
    @Range(from = 0, to = Integer.MAX_VALUE) int radius,
    @Range(from = 0, to = Integer.MAX_VALUE) int exclusionRadius
) {
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_Z = "z";
    public static final String KEY_REQUIRE_NEARBY_PLAYERS = "nearby";
    public static final String KEY_REQUIRED_NEARBY_PLAYERS_RADIUS = "checkradius";
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_EXCLUSION_RADIUS = "exclusion";

    public static @NotNull Map<@NotNull String, @NotNull CitySpawn> parse(
        final ConfigurationSection citiesSection
    ) throws ConfigParseException {
        if (citiesSection == null) {
            return new HashMap<>(0);
        }
        final var result = new HashMap<String, CitySpawn>();
        for (final String cityName : citiesSection.getKeys(false)) {
            final ConfigurationSection citySection = ConfigHelpers.get(citiesSection, cityName, ConfigValueType.SECTION);
            result.put(cityName, new CitySpawn(
                ConfigHelpers.get(citySection, KEY_X, ConfigValueType.INT),
                ConfigHelpers.get(citySection, KEY_Y, ConfigValueType.INT),
                ConfigHelpers.get(citySection, KEY_Z, ConfigValueType.INT),
                ConfigHelpers.getOrElse(citySection, KEY_REQUIRE_NEARBY_PLAYERS, ConfigValueType.BOOL, false)
                    ? ConfigHelpers.get(citySection, KEY_REQUIRED_NEARBY_PLAYERS_RADIUS, ConfigValueType.INT)
                    : null,
                ConfigHelpers.get(citySection, KEY_RADIUS, ConfigValueType.UINT),
                ConfigHelpers.getOrElse(citySection, KEY_EXCLUSION_RADIUS, ConfigValueType.UINT, 0)
            ));
        }
        return result;
    }
}
