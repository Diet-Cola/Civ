package com.github.civcraft.donum.storage;

import com.github.civcraft.donum.Donum;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public class DatabaseStorage extends IDeliveryStorage {

    public void loadDeliveryInventory(final UUID uuid) {
        Bukkit.getAsyncScheduler().runNow(Donum.getInstance(), task -> {
            ItemMap im = Donum.getManager().getDAO().getDeliveryInventory(uuid);
            Donum.getManager().setDeliveryInventory(uuid, im);
            postLoad(im, uuid);
        });
    }

    public void updateDeliveryInventory(UUID uuid, ItemMap im, boolean async) {
        if (async) {
            Bukkit.getAsyncScheduler().runNow(Donum.getInstance(), task -> Donum.getManager().getDAO().updateDeliveryInventory(uuid, im));
        } else {
            Donum.getManager().getDAO().updateDeliveryInventory(uuid, im);
        }
    }
}
