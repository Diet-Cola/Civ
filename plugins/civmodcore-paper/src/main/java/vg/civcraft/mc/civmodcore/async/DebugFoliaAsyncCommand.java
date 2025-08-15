package vg.civcraft.mc.civmodcore.async;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

@CommandAlias("debugging")
@CommandPermission("cmc.debug")
public final class DebugFoliaAsyncCommand extends BaseCommand {
    private final CivFoliaExecutors executors;

    public DebugFoliaAsyncCommand(
        final @NotNull CivModCorePlugin plugin
    ) {
        this.executors = new CivFoliaExecutors(plugin);
    }

    @Subcommand("make-all-drop-all")
    public void makeAllPlayersDropAllItems(
        final @NotNull CommandSender sender
    ) {
        CompletableFuture.supplyAsync(Bukkit::getOnlinePlayers, this.executors.global())
            .thenCompose(this.executors.entities((player) -> {
                final Inventory inventory = player.getInventory();
                final var items = new ArrayList<ItemStack>(Arrays.asList(inventory.getContents()));
                items.removeIf(ItemUtils::isEmptyItem);
                inventory.clear();
                items.forEach((item) -> player.getWorld().dropItemNaturally(player.getLocation(), item));
                player.sendMessage(Component.text("You've had a little accident...", NamedTextColor.YELLOW));
            }));
    }
}
