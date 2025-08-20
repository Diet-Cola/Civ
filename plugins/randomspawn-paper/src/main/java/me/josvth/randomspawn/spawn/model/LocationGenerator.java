package me.josvth.randomspawn.spawn.model;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface LocationGenerator {
    @Nullable Location generate(
        @NotNull World world
    );
}
