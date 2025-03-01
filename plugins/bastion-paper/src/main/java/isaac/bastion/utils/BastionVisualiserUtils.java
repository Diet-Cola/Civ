package isaac.bastion.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BastionVisualiserUtils {

    private Map<Player, ScheduledTask> taskPerPlayer;
    private Map<Player, Map<Location, BlockData>> fieldsForPlayer;
    private Map<Player, Set<BastionBlock>> bastionsNearPlayer;

    public BastionVisualiserUtils() {
        this.taskPerPlayer = new HashMap<>();
        this.fieldsForPlayer = new HashMap<>();
        this.bastionsNearPlayer = new HashMap<>();
    }

    public void startVisualTask(Player player) {
        ScheduledTask runnable = Bukkit.getAsyncScheduler().runAtFixedRate(Bastion.getPlugin(), task -> {
            showFieldsToPlayer(player);
        }, 0, 50, TimeUnit.MILLISECONDS);
        this.taskPerPlayer.put(player, runnable);
    }

    public void stopVisualTask(Player player) {
        ScheduledTask task = this.taskPerPlayer.getOrDefault(player, null);
        if (task == null) {
            return;
        }
        task.cancel();
        Bukkit.getAsyncScheduler().runDelayed(Bastion.getPlugin(), runnable -> {
            clearFieldsForPlayer(player);
        }, 50, TimeUnit.MILLISECONDS);
        this.taskPerPlayer.remove(player);
    }

    public void shutdown() {
        this.taskPerPlayer.values().forEach(ScheduledTask::cancel);
    }

    private HashMap<Location, BlockData> createFrame(Location location, Player player) {
        HashMap<Location, BlockData> blocksToChange = new HashMap<>();
        BastionBlock bastion = Bastion.getBastionStorage().getBastionBlock(location);
        int radius = bastion.getType().getEffectRadius();

        int x1 = location.getBlockX() - radius;
        int y1 = location.getBlockY();
        int z1 = location.getBlockZ() - radius;

        int x2 = x1 + (radius * 2);
        int y2 = getBlocksToSkyLimit(location);
        int z2 = z1 + (radius * 2);

        World world = location.getWorld();
            for (int xPoint = x1; xPoint <= x2; xPoint++) {
                for (int yPoint = y1; yPoint <= y2; yPoint++) {
                    Location newLoc = new Location(world, xPoint, yPoint, z1);
                    if (newLoc.getBlock().getType() != Material.AIR) {
                        continue;
                    }
                    if (bastion.inField(newLoc)) {
                        blocksToChange.put(newLoc, getMaterialForLocation(newLoc, player).createBlockData());
                    }

                }
            }
            for (int xPoint = x1; xPoint <= x2; xPoint++) {
                for (int yPoint = y1; yPoint <= y2; yPoint++) {
                    Location newLoc = new Location(world, xPoint, yPoint, z2);
                    if (newLoc.getBlock().getType() != Material.AIR) {
                        continue;
                    }
                    if (bastion.inField(newLoc)) {
                        blocksToChange.put(newLoc, getMaterialForLocation(newLoc, player).createBlockData());
                    }
                }
            }
            for (int zPoint = z1; zPoint <= z2; zPoint++) {
                for (int yPoint = y1; yPoint <= y2; yPoint++) {
                    Location newLoc = new Location(world, x1, yPoint, zPoint);
                    if (newLoc.getBlock().getType() != Material.AIR) {
                        continue;
                    }
                    if (bastion.inField(newLoc)) {
                        blocksToChange.put(newLoc, getMaterialForLocation(newLoc, player).createBlockData());
                    }
                }
            }
            for (int zPoint = z1; zPoint <= z2; zPoint++) {
                for (int yPoint = y1; yPoint <= y2; yPoint++) {
                    Location newLoc = new Location(world, x2, yPoint, zPoint);
                    if (newLoc.getBlock().getType() != Material.AIR) {
                        continue;
                    }
                    if (bastion.inField(newLoc)) {
                        blocksToChange.put(newLoc, getMaterialForLocation(newLoc, player).createBlockData());
                    }
                }
            }
        return blocksToChange;
    }

    private int getBlocksToSkyLimit(Location location){
        return location.getWorld().getMaxHeight() - location.blockY();
    }

    private void showFieldsToPlayer(Player player) {
        Set<BastionBlock> bastionBlocks = getBastionsInRenderDistance(player);
        Map<Location, BlockData> frames = getBlocksChangedForPlayer(player);
        for (BastionBlock bastion : bastionBlocks) {
            Map<Location, BlockData> frame = createFrame(bastion.getLocation(), player);
            player.sendMultiBlockChange(frame);
            frames.putAll(frame);
        }
        frames.putAll(getBlocksChangedForPlayer(player));
        this.fieldsForPlayer.put(player, frames);
    }

    private void clearFieldsForPlayer(Player player) {
        Map<Location, BlockData> changedBlocks = getBlocksChangedForPlayer(player);
        if (changedBlocks == null || changedBlocks.isEmpty()) {
            return;
        }
        changedBlocks.keySet().forEach(loc -> {
            player.sendBlockChange(loc, loc.getBlock().getBlockData());
        });
        this.fieldsForPlayer.remove(player);
    }

    private Set<BastionBlock> getBastionsInRenderDistance(Player player) {
        Set<BastionBlock> allBastions = Bastion.getBastionStorage().getAllBastions()
            .stream().filter(world -> world.getLocation().getWorld().getUID().equals(player.getWorld().getUID()))
            .collect(Collectors.toSet());
        Set<BastionBlock> nearby = getBastionsNearbyPlayer(player);
        allBastions.forEach(bastionBlock -> {
            if (bastionBlock.getLocation().distanceSquared(player.getLocation()) <= 48*48) {
                nearby.add(bastionBlock);
            } else {
                nearby.remove(bastionBlock);
            }
        });
        this.bastionsNearPlayer.put(player, nearby);
        return nearby;
    }

    private Material getMaterialForLocation(Location location, Player player) {
        //Stolen from Mode Listener
        Set<BastionBlock> bastionBlocks = Bastion.getBastionManager().getBlockingBastions(location);
        Set<BastionType> alliedBastions = new HashSet<>();
        Set<BastionType> enemyBastions = new HashSet<>();
        PermissionType placePerm = PermissionType.getPermission(Permissions.BASTION_PLACE);
        for (BastionBlock bastion : bastionBlocks) {
            if (NameAPI.getGroupManager().hasAccess(bastion.getGroup(), player.getUniqueId(), placePerm)) {
                alliedBastions.add(bastion.getType());
            } else {
                enemyBastions.add(bastion.getType());
            }
        }
        if (!alliedBastions.isEmpty() && !enemyBastions.isEmpty()) {
            return Material.YELLOW_STAINED_GLASS;
        }
        if (!alliedBastions.isEmpty()) {
            return Material.GREEN_STAINED_GLASS;
        }
        return Material.RED_STAINED_GLASS;
    }

    private Set<BastionBlock> getBastionsNearbyPlayer(Player player) {
        return this.bastionsNearPlayer.getOrDefault(player, new HashSet<>());
    }

    private Map<Location, BlockData> getBlocksChangedForPlayer(Player player) {
        return this.fieldsForPlayer.getOrDefault(player, new HashMap<>());
    }
}
