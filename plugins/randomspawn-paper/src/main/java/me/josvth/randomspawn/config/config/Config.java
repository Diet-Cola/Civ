package me.josvth.randomspawn.config.config;

import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;
import vg.civcraft.mc.civmodcore.config.ConfigValueType;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;

public record Config(
	boolean debug,
	@NotNull Messages messages,
	long damageImmunityPeriod,
	@NotNull String signText
) {
	public Config {
		Objects.requireNonNull(messages);
		Objects.requireNonNull(signText);
	}

	public static final Config DEFAULT = new Config(
		false,
		Messages.DEFAULT,
		5L,
		"[RandomSpawn]"
	);

	public static final String KEY_DEBUG = "debug";
	public static final String KEY_MESSAGES = "messages";
	public static final String KEY_DAMAGE_IMMUNITY_PERIOD = "nodamagetime";
	public static final String KEY_SIGN_TEXT = "rs-sign-text";

	public static @NotNull Config parse(
		final @NotNull ConfigurationSection configSection
	) throws ConfigParseException {
		return new Config(
			ConfigHelpers.getOrElse(configSection, KEY_DEBUG, ConfigValueType.BOOL, DEFAULT.debug()),
			Messages.parse(ConfigHelpers.get(configSection, KEY_MESSAGES, ConfigValueType.SECTION)),
			ConfigHelpers.getOrElse(configSection, KEY_DAMAGE_IMMUNITY_PERIOD, ConfigValueType.ULONG, DEFAULT.damageImmunityPeriod()),
			ConfigHelpers.get(configSection, KEY_SIGN_TEXT, ConfigValueType.STRING)
		);
	}
}
