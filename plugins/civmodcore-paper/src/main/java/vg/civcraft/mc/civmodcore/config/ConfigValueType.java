package vg.civcraft.mc.civmodcore.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.function.FailableFunction;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

public interface ConfigValueType<T> {
	default @NotNull T get(
		Object raw
	) throws ConfigParseException {
		final T val = getOrNull(raw);
		if (val == null) {
			throw ConfigParseException.isNull();
		}
		return val;
	}

	@Nullable T getOrNull(
		Object raw
	) throws ConfigParseException;

	static <T, R> @NotNull ConfigValueType<R> transform(
		final @NotNull ConfigValueType<T> fromType,
		final @NotNull FailableFunction<T, R, ConfigParseException> decoder
	) {
		Objects.requireNonNull(fromType);
		Objects.requireNonNull(decoder);
		return (raw) -> {
			final T parsed = fromType.getOrNull(raw);
			try {
				return decoder.apply(parsed);
			}
			catch (final ConfigParseException e) {
				throw e;
			}
			catch (final Exception e) {
				throw new ConfigParseException(e);
			}
		};
	}

	ConfigValueType<Boolean> BOOL = (raw) -> switch (raw) {
		case final Boolean val -> val;
		case final Object bad -> throw ConfigParseException.invalidType(Boolean.class, bad.getClass());
		case null -> null;
	};

	ConfigValueType<Integer> INT = (raw) -> switch (raw) {
		// TODO: Replace this with a primitive pattern when it's added as a language feature
		//       https://openjdk.org/jeps/507
		case final Integer val -> val;
		case final Long val when val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE -> val.intValue();
		case final Float val when val >= (float) Integer.MIN_VALUE && val <= (float) Integer.MAX_VALUE && val == Mth.floor(val) -> val.intValue();
		case final Double val when val >= (double) Integer.MIN_VALUE && val <= (double) Integer.MAX_VALUE && val == Math.floor(val) -> val.intValue();
		case final Byte val -> val.intValue();
		case final Short val -> val.intValue();
		case final Object bad -> throw ConfigParseException.invalidType(Integer.class, bad.getClass());
		case null -> null;
	};

	static @NotNull ConfigValueType<Integer> rangedInt(
		final int min,
		final int max
	) {
		if (max < min) {
			throw new IllegalArgumentException("max cannot be less than min!");
		}
		return (raw) -> {
			final Integer val = INT.getOrNull(raw);
			if (val == null) {
				return null;
			}
			if (val >= min && val <= max) {
				return val;
			}
			throw new ConfigParseException("is out of range! [" + min + ".." + max + "][" + val + "]");
		};
	}

	ConfigValueType<Long> LONG = (raw) -> switch (raw) {
		// TODO: Replace this with a primitive pattern when it's added as a language feature
		//       https://openjdk.org/jeps/507
		case final Long val -> val;
		case final Integer val -> val.longValue();
		case final Float val when val >= (float) Long.MIN_VALUE && val <= (float) Long.MAX_VALUE && val == Mth.floor(val) -> val.longValue();
		case final Double val when val >= (double) Long.MIN_VALUE && val <= (double) Long.MAX_VALUE && val == Math.floor(val) -> val.longValue();
		case final Byte val -> val.longValue();
		case final Short val -> val.longValue();
		case final Object bad -> throw ConfigParseException.invalidType(Long.class, bad.getClass());
		case null -> null;
	};

	static @NotNull ConfigValueType<Long> rangedLong(
		final long min,
		final long max
	) {
		if (max < min) {
			throw new IllegalArgumentException("max cannot be less than min!");
		}
		return (raw) -> {
			final Long val = LONG.getOrNull(raw);
			if (val == null) {
				return null;
			}
			if (val >= min && val <= max) {
				return val;
			}
			throw new ConfigParseException("is out of range! [" + min + ".." + max + "][" + val + "]");
		};
	}

	ConfigValueType<Float> FLOAT = (raw) -> switch (raw) {
		// TODO: Replace this with a primitive pattern when it's added as a language feature
		//       https://openjdk.org/jeps/507
		case final Float val -> val;
		case final Double val -> val.floatValue();
		case final Byte val -> val.floatValue();
		case final Short val -> val.floatValue();
		case final Integer val -> val.floatValue();
		case final Long val -> val.floatValue();
		case final Object bad -> throw ConfigParseException.invalidType(Long.class, bad.getClass());
		case null -> null;
	};

	ConfigValueType<Double> DOUBLE = (raw) -> switch (raw) {
		// TODO: Replace this with a primitive pattern when it's added as a language feature
		//       https://openjdk.org/jeps/507
		case final Double val -> val;
		case final Float val -> val.doubleValue();
		case final Byte val -> val.doubleValue();
		case final Short val -> val.doubleValue();
		case final Integer val -> val.doubleValue();
		case final Long val -> val.doubleValue();
		case final Object bad -> throw ConfigParseException.invalidType(Long.class, bad.getClass());
		case null -> null;
	};

	ConfigValueType<String> STRING = (raw) -> switch (raw) {
		case final Object val when ConfigHelpers.isStringlikeValue(val) -> val.toString();
		case final Object bad -> throw ConfigParseException.invalidType(String.class, bad.getClass());
		case null -> null;
	};

	ConfigValueType<ConfigurationSection> SECTION = (raw) -> switch (raw) {
		case final ConfigurationSection section -> section;
		case final Object bad -> throw ConfigParseException.invalidType(ConfigurationSection.class, bad.getClass());
		case null -> null;
	};

	static <T> @NotNull ConfigValueType<List<T>> listOf(
		final @NotNull ConfigValueType<T> elementType
	) {
		Objects.requireNonNull(elementType);
		return (raw) -> switch (raw) {
			case final List<?> list -> {
				final int length = list.size();
				final var result = new ArrayList<T>(length);
				for (int i = 0; i < length; i++) {
					final Object entry = list.get(i);
					if (entry == null) {
						throw ConfigParseException.invalidValue(null);
					}
					final T parsed;
					try {
						parsed = elementType.get(entry);
					}
					catch (final ConfigParseException e) {
						throw e.withIndex(i);
					}
					result.add(parsed);
				}
				yield result;
			}
			case final Object bad -> throw ConfigParseException.invalidType(List.class, bad.getClass());
			case null -> null;
		};
	}

	@SuppressWarnings("unchecked")
	static <T extends ConfigurationSerializable> @NotNull ConfigValueType<T> serialised(
		final @NotNull Class<T> type
	) {
		Objects.requireNonNull(type);
		return (raw) -> switch (raw) {
			case final Object obj when type.isAssignableFrom(obj.getClass()) -> (T) obj;
			case final Object bad -> throw ConfigParseException.invalidType(List.class, bad.getClass());
			case null -> null;
		};
	}

	// ============================================================
	// Premade shortcuts
	//
 	// These exist not just as conveniences, but also as a means
	// to show how the APIs are meant to be used.
	// ============================================================

	/// Convenience shortcut
	ConfigValueType<Integer> UINT = rangedInt(0, Integer.MAX_VALUE);

	/// Convenience shortcut
	ConfigValueType<Long> ULONG = rangedLong(0L, Long.MAX_VALUE);

	/// Convenience shortcut
	ConfigValueType<List<String>> STRING_LIST = listOf(STRING);

	/// Convenience shortcut
	ConfigValueType<Material> MATERIAL = transform(STRING, MaterialUtils::getMaterial);

	/// Convenience shortcut
	ConfigValueType<List<Material>> MATERIAL_LIST = listOf(MATERIAL);

	/// Convenience shortcut
	ConfigValueType<Component> LEGACY_MESSAGE = transform(STRING, LegacyComponentSerializer.legacySection()::deserialize);

	/// Convenience shortcut
	ConfigValueType<Component> MINI_MESSAGE = transform(STRING, MiniMessage.miniMessage()::deserialize);
}
