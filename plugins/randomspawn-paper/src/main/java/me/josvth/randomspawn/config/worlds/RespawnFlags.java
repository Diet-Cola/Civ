package me.josvth.randomspawn.config.worlds;

import java.util.Set;
import org.apache.commons.collections4.IterableUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;
import vg.civcraft.mc.civmodcore.config.ConfigValueType;

/// @param reviveWithoutBed When someone revives but had no bed (or other respawn position) set.
/// @param reviveWithBed When someone revives with a bed (or other respawn position) set.
/// @param firstJoin When someone joins the server for the first time and needs to be randomly entered into the world.
/// @param reviveWhileNew When someone revives and is still considered a "new player".
/// @param teleportsFrom When someone has teleported from particular worlds.
public record RespawnFlags(
    boolean reviveWithoutBed,
    boolean reviveWithBed,
    boolean firstJoin,
    boolean reviveWhileNew,
    @NotNull Set<@NotNull String> teleportsFrom
) {
    public RespawnFlags {
        teleportsFrom = Set.copyOf(teleportsFrom);
    }

    public static final RespawnFlags DEFAULT = new RespawnFlags(
        false,
        false,
        false,
        false,
        Set.of()
    );

    public static final String KEY_REVIVE_WITHOUT_BED = "reviveWithoutBed";
    public static final String KEY_REVIVE_WITH_BED = "reviveWithBed";
    public static final String KEY_FIRST_JOIN = "firstJoin";
    public static final String KEY_REVIVE_WHILE_NEW = "reviveWhileNew";
    public static final String KEY_TELEPORTS_FROM = "teleportsFrom";

    static @NotNull RespawnFlags parse(
        final ConfigurationSection flagsSection
    ) throws ConfigParseException {
        if (flagsSection == null) {
            return DEFAULT;
        }
        return new RespawnFlags(
            ConfigHelpers.getOrElse(flagsSection, KEY_REVIVE_WITHOUT_BED, ConfigValueType.BOOL, DEFAULT.reviveWithoutBed()),
            ConfigHelpers.getOrElse(flagsSection, KEY_REVIVE_WITH_BED, ConfigValueType.BOOL, DEFAULT.reviveWithBed()),
            ConfigHelpers.getOrElse(flagsSection, KEY_FIRST_JOIN, ConfigValueType.BOOL, DEFAULT.firstJoin()),
            ConfigHelpers.getOrElse(flagsSection, KEY_REVIVE_WHILE_NEW, ConfigValueType.BOOL, DEFAULT.reviveWhileNew()),
            Set.copyOf(ConfigHelpers.getOrElse(flagsSection, KEY_TELEPORTS_FROM, ConfigValueType.STRING_LIST, IterableUtils.toList(DEFAULT.teleportsFrom())))
        );
    }
}
