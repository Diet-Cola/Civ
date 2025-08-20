package me.josvth.randomspawn.config.worlds;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;
import vg.civcraft.mc.civmodcore.config.ConfigValueType;

public record SpawnByPlayerArea(
    @Range(from = 0, to = Integer.MAX_VALUE) int radius,
    @Range(from = 0, to = Integer.MAX_VALUE) int exclusionRadius
) {
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_EXCLUSION_RADIUS = "exclusionradius";

    public static @NotNull SpawnByPlayerArea parse(
        final @NotNull ConfigurationSection spawnByPlayerAreaSection
    ) throws ConfigParseException {
        return new SpawnByPlayerArea(
            ConfigHelpers.get(spawnByPlayerAreaSection, KEY_RADIUS, ConfigValueType.UINT),
            ConfigHelpers.getOrElse(spawnByPlayerAreaSection, KEY_EXCLUSION_RADIUS, ConfigValueType.UINT, 0)
        );
    }
}
