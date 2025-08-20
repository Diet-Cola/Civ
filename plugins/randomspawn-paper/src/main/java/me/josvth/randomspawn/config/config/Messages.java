package me.josvth.randomspawn.config.config;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;
import vg.civcraft.mc.civmodcore.config.ConfigValueType;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;

public record Messages(
	@NotNull Component randomSpawned
) {
	public Messages {
		Objects.requireNonNull(randomSpawned);
	}

	public static final Messages DEFAULT = new Messages(
		Component.text("You wake up in an unfamiliar place.")
	);

	public static final String KEY_RANDOM_SPAWN_MESSAGES = "randomspawned";

	public static @NotNull Messages parse(
		final @NotNull ConfigurationSection messagesSection
	) throws ConfigParseException {
		return new Messages(
			ConfigHelpers.getOrElse(messagesSection, KEY_RANDOM_SPAWN_MESSAGES, ConfigValueType.LEGACY_MESSAGE, DEFAULT.randomSpawned())
		);
	}
}
