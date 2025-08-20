package vg.civcraft.mc.civmodcore.async;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.IntegerRange;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

@CommandAlias("debugging")
@CommandPermission("cmc.debug")
public final class FoliaTestCommand extends BaseCommand {
    private final CivModCorePlugin plugin;

    public FoliaTestCommand(
        final @NotNull CivModCorePlugin plugin
    ) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    @Subcommand("randomly-teleport-everyone")
    public void randomlyTeleportEveryone(
        final @NotNull CommandSender sender
    ) {
        VirtualExecutor.get(this.plugin).execute(() -> {
            final List<Player> players = TaskThread.globalRegion(this.plugin)
                .awaitGet(() -> List.copyOf(Bukkit.getOnlinePlayers()));

            for (final Player player : players) {
                sender.sendMessage(Component.textOfChildren(
                    Component.text("Generating random chunk location for [", NamedTextColor.YELLOW),
                    player.name(),
                    Component.text("]!", NamedTextColor.YELLOW)
                ));

                final World world = TaskThread.entity(this.plugin, player)
                    .awaitApply(player, Entity::getWorld);
                final int chunkX, chunkZ; {
                    final Random random = ThreadLocalRandom.current();
                    chunkX = random.nextInt(-310, 310); // 4960
                    chunkZ = random.nextInt(-310, 310); // 4960
                }

                sender.sendMessage(Component.text("Random chunk (" + chunkX + "," + chunkZ + ")! Now loading...", NamedTextColor.YELLOW));
                final Chunk chunk = TaskThread.region(this.plugin, world, chunkX, chunkZ)
                    .awaitGet(() -> world.getChunkAtAsync(chunkX, chunkZ, true))
                    .join();
                sender.sendMessage(Component.text("Random chunk (" + chunkX + "," + chunkZ + ") loaded!", NamedTextColor.YELLOW));

                final Location location = TaskThread.region(this.plugin, world, chunkX, chunkZ)
                    .awaitGet(() -> WorldUtils.findValidSpawnInChunk(
                        chunk,
                        IntegerRange.of(world.getMinHeight(), world.getMaxHeight() - 1),
                        true,
                        Set.of()
                    ));
                if (location == null) {
                    sender.sendMessage(Component.text("Could not generate a position within chunk (" + chunk.getX() + "," + chunk.getZ() + ")", NamedTextColor.RED));
                    continue;
                }
                
                player.sendMessage(Component.text("You're about to be teleported...", NamedTextColor.YELLOW));
                if (player.teleportAsync(location).join()) {
                    player.sendMessage(Component.text("You've been teleported!", NamedTextColor.GREEN));
                    if (player != sender) {
                        sender.sendMessage(Component.textOfChildren(
                            player.name(),
                            Component.text(" has been teleported!", NamedTextColor.GREEN)
                        ));
                    }
                }
                else {
                    player.sendMessage(Component.text("Teleport failed!", NamedTextColor.RED));
                    if (player != sender) {
                        sender.sendMessage(Component.textOfChildren(
                            player.name(),
                            Component.text(" failed to teleport!", NamedTextColor.RED)
                        ));
                    }
                }
            }

            sender.sendMessage(Component.text("Done.", NamedTextColor.GREEN));
        });
    }
}
