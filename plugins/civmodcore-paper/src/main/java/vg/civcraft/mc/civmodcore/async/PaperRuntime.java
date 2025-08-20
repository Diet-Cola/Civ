package vg.civcraft.mc.civmodcore.async;

import org.jetbrains.annotations.NotNull;

public enum PaperRuntime {
    PAPER,
    FOLIA;

    public static final PaperRuntime DETECTED_RUNTIME = detectRuntime();
    private static @NotNull PaperRuntime detectRuntime() {
        // Got this from a pinned message in the #folia-dev PaperMC discord channel:
        // https://discord.com/channels/289587909051416579/1090729705222721637/1091507602829082644
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return PaperRuntime.FOLIA;
        }
        catch (final ClassNotFoundException ignored) {
            return PaperRuntime.PAPER;
        }
    }

    public static boolean isFolia() {
        return DETECTED_RUNTIME == PaperRuntime.FOLIA;
    }
}
