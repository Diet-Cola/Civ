package me.josvth.randomspawn.config.worlds;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;
import vg.civcraft.mc.civmodcore.config.ConfigValueType;

public sealed interface RandomSpawnArea {
    record Circle(
        int centreX,
        int centreZ,
        @Range(from = 0, to = Integer.MAX_VALUE) int radius,
        @Range(from = 0, to = Integer.MAX_VALUE) int exclusionRadius
    ) implements RandomSpawnArea {}

    record Square(
        int minX,
        int minZ,
        int maxX,
        int maxZ,
        @Range(from = 0, to = Integer.MAX_VALUE) int thickness // uwu
    ) implements RandomSpawnArea {
        public Square {
            final int actualMinX = Math.min(minX, maxX);
            final int actualMinZ = Math.min(minZ, maxZ);
            final int actualMaxX = Math.max(minX, maxX);
            final int actualMaxZ = Math.max(minZ, maxZ);
            minX = actualMinX;
            maxX = actualMaxX;
            minZ = actualMinZ;
            maxZ = actualMaxZ;
        }
    }

    String KEY_TYPE = "type";
    String KEY_CIRCLE_TYPE = "circle";
    String KEY_CIRCLE_CENTRE_X = "xcenter";
    String KEY_CIRCLE_CENTRE_Z = "zcenter";
    String KEY_CIRCLE_RADIUS = "radius";
    String KEY_CIRCLE_EXCLUSION_RADIUS = "exclusionradius";
    String KEY_SQUARE_TYPE = "square";
    String KEY_SQUARE_MIN_X = "x-min";
    String KEY_SQUARE_MAX_X = "x-max";
    String KEY_SQUARE_MIN_Z = "z-min";
    String KEY_SQUARE_MAX_Z = "z-max";
    String KEY_SQUARE_THICKNESS = "thickness";

    static @NotNull RandomSpawnArea parse(
        final @NotNull ConfigurationSection spawnAreaSection
    ) throws ConfigParseException {
        return switch (ConfigHelpers.get(spawnAreaSection, KEY_TYPE, ConfigValueType.STRING)) {
            case KEY_CIRCLE_TYPE -> new Circle(
                ConfigHelpers.getOrElse(spawnAreaSection, KEY_CIRCLE_CENTRE_X, ConfigValueType.INT, 0),
                ConfigHelpers.getOrElse(spawnAreaSection, KEY_CIRCLE_CENTRE_Z, ConfigValueType.INT, 0),
                ConfigHelpers.get(spawnAreaSection, KEY_CIRCLE_RADIUS, ConfigValueType.UINT),
                ConfigHelpers.getOrElse(spawnAreaSection, KEY_CIRCLE_EXCLUSION_RADIUS, ConfigValueType.UINT, 0)
            );
            case KEY_SQUARE_TYPE -> new Square(
                ConfigHelpers.get(spawnAreaSection, KEY_SQUARE_MIN_X, ConfigValueType.INT),
                ConfigHelpers.get(spawnAreaSection, KEY_SQUARE_MIN_Z, ConfigValueType.INT),
                ConfigHelpers.get(spawnAreaSection, KEY_SQUARE_MAX_X, ConfigValueType.INT),
                ConfigHelpers.get(spawnAreaSection, KEY_SQUARE_MAX_Z, ConfigValueType.INT),
                ConfigHelpers.getOrElse(spawnAreaSection, KEY_SQUARE_THICKNESS, ConfigValueType.UINT, 0)
            );
            case final String invalid -> throw ConfigParseException.invalidValue(invalid).withPath(spawnAreaSection, KEY_TYPE);
        };
    }
}
