package vg.civcraft.mc.civmodcore.config.errors;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ConfigFileException extends Exception {
    public ConfigFileException() {
        super();
    }

    public ConfigFileException(
        final @NotNull String message
    ) {
        super(Objects.requireNonNull(message));
    }

    public ConfigFileException(
        final @NotNull Throwable cause
    ) {
        super(Objects.requireNonNull(cause));
    }

    public ConfigFileException(
        final @NotNull String message,
        final @NotNull Throwable cause
    ) {
        super(
            Objects.requireNonNull(message),
            Objects.requireNonNull(cause)
        );
    }
}
