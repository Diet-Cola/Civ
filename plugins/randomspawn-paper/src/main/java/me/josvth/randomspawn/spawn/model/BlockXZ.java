package me.josvth.randomspawn.spawn.model;

public record BlockXZ(
    int blockX,
    int blockZ
) {
    /// Convenience shortcut
    public BlockXZ(
        final double blockX,
        final double blockZ
    ) {
        this(
            (int) blockX,
            (int) blockZ
        );
    }
}
