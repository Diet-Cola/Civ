package vg.civcraft.mc.civmodcore.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.function.FailableFunction;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

/// Use [org.apache.commons.lang3.function.Failable] to wrap config parsing.
public final class ConfigHelpers {
    public static @NotNull String pathOf(
        @NotNull String @NotNull ... parts
    ) {
        return String.join(".", parts);
    }

    public static <T> T parse(
        final @NotNull ConfigurationSection parent,
        final @NotNull String path,
        final @NotNull FailableFunction<Object, T, ConfigParseException> parser
    ) throws ConfigParseException {
        try {
            return parser.apply(parent.get(path, null));
        }
        catch (final ConfigParseException e) {
            throw e.withPath(parent, path);
        }
    }

    public static <T> @NotNull T get(
        final @NotNull ConfigurationSection parent,
        final @NotNull String path,
        final @NotNull ConfigValueType<T> type
    ) throws ConfigParseException {
        final T value = getOrNull(parent, path, type);
        if (value == null) {
            throw ConfigParseException.isNull().withPath(parent, path);
        }
        return value;
    }

    public static <T> @Nullable T getOrNull(
        final @NotNull ConfigurationSection parent,
        final @NotNull String path,
        final @NotNull ConfigValueType<T> type
    ) throws ConfigParseException {
        try {
            return type.getOrNull(parent.get(path, null));
        }
        catch (final ConfigParseException e) {
            throw e.withPath(parent, path);
        }
    }

    public static <T> @NotNull T getOrElse(
        final @NotNull ConfigurationSection parent,
        final @NotNull String path,
        final @NotNull ConfigValueType<T> type,
        final @NotNull T defaultValue
    ) throws ConfigParseException {
        Objects.requireNonNull(defaultValue);
        final T value;
        try {
            value = type.getOrNull(parent.get(path, null));
        }
        catch (final ConfigParseException e) {
            throw e.withPath(parent, path);
        }
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static <T> @NotNull T getOrElseGet(
        final @NotNull ConfigurationSection parent,
        final @NotNull String path,
        final @NotNull ConfigValueType<T> type,
        final @NotNull Supplier<T> defaultGetter
    ) throws ConfigParseException {
        Objects.requireNonNull(defaultGetter);
        final T value;
        try {
            value = type.getOrNull(parent.get(path, null));
        }
        catch (final ConfigParseException e) {
            throw e.withPath(parent, path);
        }
        if (value == null) {
            return Objects.requireNonNull(defaultGetter.get());
        }
        return value;
    }

    public static boolean remove(
        final @NotNull ConfigurationSection parent,
        final @NotNull String path
    ) {
        if (parent.contains(path, true)) {
            parent.set(path, null);
            return true;
        }
        return false;
    }

    /// This *should* include Dates, but it's not possible to retrieve these from [ConfigurationSection] so I think the
    /// point is moot. Nor can I imagine any kind of use case where you'd be storing a list of ISO-8601 dates in a YAML
    /// file.
    @Contract("null -> false")
    public static boolean isStringlikeValue(
        final Object value
    ) {
        return switch (value) {
            case String $ -> true;
            case Number $ -> true;
            case Boolean $ -> true;
            case Character $ -> true;
            case null, default -> false;
        };
    }

    /// @apiNote Keep in mind this will override any value that already exists at that key.
    public static @NotNull ConfigurationSection ensureSection(
        final @NotNull ConfigurationSection parent,
        final @NotNull String key
    ) {
        return switch (parent.get(key, null)) {
            case final ConfigurationSection section -> section;
            case null, default -> parent.createSection(key);
        };
    }

    /// @deprecated Use [#get(ConfigurationSection, String, ConfigValueType)] with [ConfigValueType#STRING_LIST] instead.
    @Deprecated
    public static @NotNull List<@NotNull String> getStringList(
        final @NotNull ConfigurationSection parent,
        final @NotNull String key
    ) {
        return switch (parent.get(key, null)) {
            case final List<?> list -> {
                final var result = new ArrayList<String>(list.size());
                for (final Object entry : list) {
                    if (isStringlikeValue(entry)) {
                        result.add(entry.toString());
                    }
                }
                yield result;
            }
            case final Object single when isStringlikeValue(single) -> new ArrayList<>(
                Collections.singletonList(single.toString())
            );
            case null, default -> new ArrayList<>(0);
        };
    }

    /// @deprecated Use [#get(ConfigurationSection, String, ConfigValueType)] with [ConfigValueType#MATERIAL_LIST] instead.
    @Deprecated
    public static @NotNull List<@NotNull Material> getSafeMaterialList(
        final @NotNull ConfigurationSection parent,
        final @NotNull String key
    ) {
        return getStringList(parent, key)
            .stream()
            .map(MaterialUtils::getMaterial)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Creates an item map containing all the items listed in the given config
     * section
     *
     * @param config ConfigurationSection to parse the items from
     * @return The item map created
     */
    @NotNull
    public static ItemMap parseItemMap(@Nullable final ConfigurationSection config) {
        final var result = new ItemMap();
        if (config == null) {
            return result;
        }
        for (final String key : config.getKeys(false)) {
            ItemMap partMap = new ItemMap();
            ConfigurationSection section = config.getConfigurationSection(key);
            String custom = section == null ? null : section.getString("custom-key");
            if (custom != null) {
                ItemStack item = CustomItem.getCustomItem(custom);
                if (item == null) {
                    throw new IllegalArgumentException("Unknown custom item key " + custom);
                } else {
                    int amount = section.getInt("amount", 1);
                    partMap.addItemAmount(item, amount);
                }
            } else {
                partMap.addItemStack(config.getItemStack(key, ItemStack.empty()));
            }
            result.merge(partMap);
        }
        return result;
    }

    public static int parseTimeAsTicks(@NotNull final String arg) {
        return (int) (parseTime(arg, TimeUnit.MILLISECONDS) / 50L);
    }

    public static long parseTime(@NotNull final String arg,
                                 @NotNull final TimeUnit unit) {
        long millis = parseTime(arg);
        return unit.convert(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Parses a time value specified in a config. This allows to specify human
     * readable time values easily, instead of having to specify every amount in
     * ticks or seconds. The unit of a number specifed by the letter added after it,
     * for example 5h means 5 hours or 34s means 34 seconds. Possible modifiers are:
     * t (ticks), s (seconds), m (minutes), h (hours) and d (days)
     * <p>
     * Additionally you can combine those amounts in any way you want, for example
     * you can specify 3h5m43s as 3 hours, 5 minutes and 43 seconds. This doesn't
     * have to be sorted and may even list the same unit multiple times for
     * different values, but the values are not allowed to be separated by anything
     *
     * @param input Parsed string containing the time format
     * @return How many milliseconds the given time value is
     */
    public static long parseTime(@NotNull String input) {
        input = input.replace(" ", "").replace(",", "").toLowerCase();
        long result = 0;
        try {
            result += Long.parseLong(input);
            return result;
        } catch (NumberFormatException e) {
        }
        while (!input.equals("")) {
            String typeSuffix = getSuffix(input, Character::isLetter);
            input = input.substring(0, input.length() - typeSuffix.length());
            String numberSuffix = getSuffix(input, Character::isDigit);
            input = input.substring(0, input.length() - numberSuffix.length());
            long duration;
            if (numberSuffix.length() == 0) {
                duration = 1;
            } else {
                duration = Long.parseLong(numberSuffix);
            }
            switch (typeSuffix) {
                case "ms":
                case "milli":
                case "millis":
                    result += duration;
                    break;
                case "s": // seconds
                case "sec":
                case "second":
                case "seconds":
                    result += TimeUnit.SECONDS.toMillis(duration);
                    break;
                case "m": // minutes
                case "min":
                case "minute":
                case "minutes":
                    result += TimeUnit.MINUTES.toMillis(duration);
                    break;
                case "h": // hours
                case "hour":
                case "hours":
                    result += TimeUnit.HOURS.toMillis(duration);
                    break;
                case "d": // days
                case "day":
                case "days":
                    result += TimeUnit.DAYS.toMillis(duration);
                    break;
                case "w": // weeks
                case "week":
                case "weeks":
                    result += TimeUnit.DAYS.toMillis(duration * 7);
                    break;
                case "month": // weeks
                case "months":
                    result += TimeUnit.DAYS.toMillis(duration * 30);
                    break;
                case "y":
                case "year":
                case "years":
                    result += TimeUnit.DAYS.toMillis(duration * 365);
                    break;
                case "never":
                case "inf":
                case "infinite":
                case "perm":
                case "perma":
                case "forever":
                    // 1000 years counts as perma
                    result += TimeUnit.DAYS.toMillis(365 * 1000);
                default:
                    // just ignore it
            }
        }
        return result;
    }

    @NotNull
    private static String getSuffix(@NotNull final String arg,
                                    @NotNull final Predicate<Character> selector) {
        StringBuilder number = new StringBuilder();
        for (int i = arg.length() - 1; i >= 0; i--) {
            if (selector.test(arg.charAt(i))) {
                number.insert(0, arg.substring(i, i + 1));
            } else {
                break;
            }
        }
        return number.toString();
    }

    /**
     * Parses a section which contains key-value mappings of a type to another type
     *
     * @param <K>            Key type
     * @param <V>            Value type
     * @param parent         Configuration section containing the section with the values
     * @param key            Config identifier of the section containing the entries
     * @param logger         The logger to write in progress work to
     * @param keyConverter   Converts strings to type K
     * @param valueConverter Converts strings to type V
     * @param mapToUse       The map to place parsed keys and values.
     */
    public static <K, V> void parseKeyValueMap(
        final @NotNull ConfigurationSection parent,
        final @NotNull String key,
        final @NotNull Logger logger,
        final @NotNull Function<String, K> keyConverter,
        final @NotNull Function<String, V> valueConverter,
        final @NotNull Map<K, V> mapToUse
    ) {
        if (!(parent.get(key, null) instanceof final ConfigurationSection section)) {
            return;
        }
        for (String keyString : section.getKeys(false)) {
            if (section.isConfigurationSection(keyString)) {
                logger.warn("Ignoring invalid {} entry {} at {}", key, keyString, section.getCurrentPath());
                continue;
            }
            K keyinstance;
            try {
                keyinstance = keyConverter.apply(keyString);
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to parse {} {} at {}: {}", key, keyString, section.getCurrentPath(), e);
                continue;
            }
            V value = valueConverter.apply(section.getString(keyString));
            mapToUse.put(keyinstance, value);
        }
    }
}
