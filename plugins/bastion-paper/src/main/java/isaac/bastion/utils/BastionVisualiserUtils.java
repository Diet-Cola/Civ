package isaac.bastion.utils;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.Permissions;
import it.unimi.dsi.fastutil.Hash;
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

public class BastionVisualiserUtils {

    private Map<Player, Map<Location, BlockData>> fieldsForPlayer;
    private Map<Player, Set<BastionBlock>> bastionsNearPlayer;

    public BastionVisualiserUtils() {
        this.fieldsForPlayer = new HashMap<>();
        this.bastionsNearPlayer = new HashMap<>();
    }

    public HashMap<Location, BlockData> buildFrameAroundBastionLocation(Location bastion, Player player) {
        HashMap<Location, BlockData> blocksToChange = new HashMap<>();
        int radius = Bastion.getBastionStorage().getBastionBlock(bastion).getType().getEffectRadius();

        int x1 = bastion.getBlockX() - radius;
        int y1 = bastion.getBlockY();
        int z1 = bastion.getBlockZ() - radius;

        int x2 = x1 + (radius * 2);
        int y2 = getBlocksToSkyLimit(bastion);
        int z2 = z1 + (radius * 2);

        World world = bastion.getWorld();
            for (int xPoint = x1; xPoint <= x2; xPoint++) {
                for (int yPoint = y1; yPoint <= y2; yPoint++) {
                    Location newLoc = new Location(world, xPoint, yPoint, z1);
                    blocksToChange.put(newLoc, getMaterialForLocation(newLoc, player).createBlockData());
                }
            }
            for (int xPoint = x1; xPoint <= x2; xPoint++) {
                for (int yPoint = y1; yPoint <= y2; yPoint++) {
                    Location newLoc = new Location(world, xPoint, yPoint, z2);
                    blocksToChange.put(newLoc, getMaterialForLocation(newLoc, player).createBlockData());
                }
            }
            for (int zPoint = z1; zPoint <= z2; zPoint++) {
                for (int yPoint = y1; yPoint <= y2; yPoint++) {
                    Location newLoc = new Location(world, x1, yPoint, zPoint);
                    blocksToChange.put(newLoc, getMaterialForLocation(newLoc, player).createBlockData());
                }
            }
            for (int zPoint = z1; zPoint <= z2; zPoint++) {
                for (int yPoint = y1; yPoint <= y2; yPoint++) {
                    Location newLoc = new Location(world, x2, yPoint, zPoint);
                    blocksToChange.put(newLoc, getMaterialForLocation(newLoc, player).createBlockData());
                }
            }
            blocksToChange.keySet().stream().filter(Location::isChunkLoaded);
        return blocksToChange;
    }

    public int getBlocksToSkyLimit(Location location){
        return location.getWorld().getMaxHeight() - location.blockY();
    }

    public void showFieldToPlayer(Player player) {
        Bukkit.getAsyncScheduler().runNow(Bastion.getPlugin(), task -> {
            Set<BastionBlock> bastionBlocks = getBastionsInRenderDistance(player, player.getLocation());
            for (BastionBlock bastion : bastionBlocks) {
                Map<Location, BlockData> frame = buildFrameAroundBastionLocation(bastion.getLocation(), player);
                player.sendMultiBlockChange(frame);
                getBlocksChangedForPlayer(player).putAll(frame);
            }
        });
    };

    public void clearFieldsForPlayer(Player player) {
        Map<Location, BlockData> changedBlocks = getBlocksChangedForPlayer(player);
        if (changedBlocks == null || changedBlocks.isEmpty()) {
            return;
        }
        Set<Location> locs = changedBlocks.keySet();
        Bukkit.getAsyncScheduler().runNow(Bastion.getPlugin(), task -> {
            for (Location block : locs) {
                player.sendBlockChange(block, block.getBlock().getBlockData());
                changedBlocks.remove(block);
            }
        });
    }
    
    public Set<BastionBlock> getBastionsInRenderDistance(Player player, Location location) {
        Set<BastionBlock> allBastions = Bastion.getBastionStorage().getAllBastions();
        Set<BastionBlock> nearby = getBastionsNearbyPlayer(player);
        Bukkit.getAsyncScheduler().runNow(Bastion.getPlugin(), scheduledTask -> {
            allBastions.forEach(bastionBlock -> {
                if (bastionBlock.getLocation().distanceSquared(location) <= 48*48) {
                    nearby.add(bastionBlock);
                } else {
                    if (nearby.contains(bastionBlock)) {
                        nearby.remove(bastionBlock);
                        this.fieldsForPlayer.remove(player);
                    }
                }
            });
        });
        return nearby;
    }

    public Set<BastionBlock> getBastionsNearbyPlayer(Player player) {
        return this.bastionsNearPlayer.getOrDefault(player, new HashSet<>());
    }

    public Map<Location, BlockData> getBlocksChangedForPlayer(Player player) {
        return this.fieldsForPlayer.getOrDefault(player, new HashMap<>());
    }

    public Material getMaterialForLocation(Location location, Player player) {
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
}
