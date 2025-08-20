package me.josvth.randomspawn.config.worlds;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;
import vg.civcraft.mc.civmodcore.config.ConfigValueType;

public record FirstSpawn(
	int x,
	int y,
	int z,
	float yaw,
	float pitch
) {
	public static final String KEY_X = "x";
	public static final String KEY_Y = "y";
	public static final String KEY_Z = "z";
	public static final String KEY_YAW = "yaw";
	public static final String KEY_PITCH = "pitch";

	public static @Nullable FirstSpawn parse(
		final ConfigurationSection firstSpawnSection
	) throws ConfigParseException {
		if (firstSpawnSection == null) {
			return null;
		}
		return new FirstSpawn(
			ConfigHelpers.getOrElse(firstSpawnSection, KEY_X, ConfigValueType.INT, 0),
			ConfigHelpers.getOrElse(firstSpawnSection, KEY_Y, ConfigValueType.INT, 0),
			ConfigHelpers.getOrElse(firstSpawnSection, KEY_Z, ConfigValueType.INT, 0),
			ConfigHelpers.getOrElse(firstSpawnSection, KEY_YAW, ConfigValueType.FLOAT, 0f),
			ConfigHelpers.getOrElse(firstSpawnSection, KEY_PITCH, ConfigValueType.FLOAT, 0f)
		);
	}
}
