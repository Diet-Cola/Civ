package vg.civcraft.mc.civmodcore.config.errors;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class ConfigParseException extends Exception {
    public static final String UNKNOWN_PATH = "<unknown>";

    private String path = UNKNOWN_PATH;
    private Integer index = null;

    public ConfigParseException(
        final @NotNull String message
    ) {
        super(Objects.requireNonNull(message));
    }

    public ConfigParseException(
        final @NotNull Throwable cause
    ) {
        super(Objects.requireNonNull(cause));
    }

    public @NotNull String getFullPath() {
        return this.path + (this.index instanceof final Integer i ? "[" + i + "]" : "");
    }

    @Contract("_, _ -> this")
    public @NotNull ConfigParseException withPath(
        final @NotNull ConfigurationSection parent,
        final @NotNull String key
    ) {
        String path = parent.getCurrentPath();
        if (StringUtils.isBlank(path)) {
            path = UNKNOWN_PATH;
        }
        if (!key.isBlank()) {
            path += "." + key;
        }
        this.path = path;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ConfigParseException withIndex(
        final @Range(from = 0, to = Integer.MAX_VALUE) int index
    ) {
        this.index = index;
        return this;
    }

    @Override
    public @NotNull String getMessage() {
        return getFullPath() + ": " + super.getMessage();
    }

    public @NotNull String getJustMessage() {
        return super.getMessage();
    }

    /// Convenience shortcut
    public static @NotNull ConfigParseException isNull() {
        return new ConfigParseException("cannot be null!");
    }

    /// Convenience shortcut
    public static @NotNull ConfigParseException invalidType(
        final @NotNull Class<?> expectedType,
        final @NotNull Class<?> actualType
    ) {
        return new ConfigParseException("invalid type! Must be [" + expectedType.getName() + "] Got [" + actualType.getName() + "]");
    }

    /// Convenience shortcut
    /// @apiNote Use this when it's a valid type but not the correct value, eg: an incorrect enum name.
    public static @NotNull ConfigParseException invalidValue(
        final Object invalid
    ) {
        return new ConfigParseException("invalid value! [" + Objects.toString(invalid, "null") + "]");
    }
}
