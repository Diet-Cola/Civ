package com.github.maxopoly.finale.misc;

import com.github.maxopoly.finale.Finale;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class SaturationHealthRegenHandler {

    private List<LinkedList<UUID>> ticks;
    private Map<UUID, Integer> tickMapping;
    private int currentTick;
    private double healthPerCycle;
    private int minimumFood;
    private float exhaustionPerHeal;
    private int interval;
    private ScheduledTask task;
    private boolean blockPassiveHealthRegen;
    private boolean blockFoodHealthRegen;

    public SaturationHealthRegenHandler(int interval, double healthPerCycle, int minimumFood, float exhaustionPerHeal,
                                        boolean blockPassiveHealthRegen, boolean blockFoodHealthRegen) {
        this.currentTick = 0;
        this.ticks = new ArrayList<>(interval);
        for (int i = 0; i < interval; i++) {
            ticks.add(null);
        }
        tickMapping = new TreeMap<>();
        this.interval = interval;
        this.healthPerCycle = healthPerCycle;
        this.minimumFood = minimumFood;
        this.exhaustionPerHeal = exhaustionPerHeal;
        this.task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(Finale.getPlugin(), saturationTask -> {
            this.run();
        }, 1L, 1L);
        this.blockPassiveHealthRegen = blockPassiveHealthRegen;
        this.blockFoodHealthRegen = blockFoodHealthRegen;
    }

    public boolean blockFoodHealthRegen() {
        return blockFoodHealthRegen;
    }

    public boolean blockPassiveHealthRegen() {
        return blockPassiveHealthRegen;
    }

    public float getExhaustionPerHeal() {
        return exhaustionPerHeal;
    }

    public double getHealthPerHeal() {
        return healthPerCycle;
    }

    public int getInterval() {
        return interval;
    }

    public int getMinimumFood() {
        return minimumFood;
    }

    public ScheduledTask getTask() {
        return task;
    }

    public void registerPlayer(UUID uuid) {
        LinkedList<UUID> players = ticks.get(currentTick);
        if (players == null) {
            players = new LinkedList<>();
            ticks.set(currentTick, players);
        }
        Integer exisTick = tickMapping.get(uuid);
        if (exisTick != null) {
            LinkedList<UUID> exis = ticks.get(exisTick);
            exis.remove(uuid);
        }
        tickMapping.put(uuid, currentTick);
        players.add(uuid);
    }

    public void run() {
        LinkedList<UUID> players = ticks.get(currentTick);
        if (players != null) {
            ListIterator<UUID> iter = players.listIterator();
            while (iter.hasNext()) {
                UUID player = iter.next();
                Player p = Bukkit.getPlayer(player);
                if (p == null) {
                    // player is offline?
                    iter.remove();
                    return;
                }
                p.getScheduler().run(Finale.getPlugin(), feedTask -> {
                    if (p.isDead() || p.getHealth() <= 0.0) {
                        return;
                    }
                    double maxHealth = p.getAttribute(Attribute.MAX_HEALTH).getValue();
                    if (p.getFoodLevel() >= minimumFood && p.getHealth() < maxHealth) {
                        StringBuilder alterHealth = null;

                        if (Finale.getPlugin().getManager().isDebug()) {
                            alterHealth = new StringBuilder(p.getName());
                            alterHealth.append(":").append(p.getHealth()).append("<").append(maxHealth);
                            alterHealth.append(":").append(p.getSaturation()).append(":").append(p.getExhaustion());
                            alterHealth.append(":").append(p.getFoodLevel());
                        }
                        double newHealth = p.getHealth() + healthPerCycle;
                        newHealth = Math.min(newHealth, maxHealth);
                        p.setExhaustion(p.getExhaustion() + exhaustionPerHeal);
                        p.setHealth(newHealth);
                        if (Finale.getPlugin().getManager().isDebug()) {
                            alterHealth.append(" TO ").append(p.getHealth()).append("<").append(maxHealth);
                            alterHealth.append(":").append(p.getSaturation()).append(":").append(p.getExhaustion());
                            alterHealth.append(":").append(p.getFoodLevel());
                            Finale.getPlugin().getLogger().info(alterHealth.toString());
                        }
                    }
                }, null);
            }
        }
        currentTick++;
        if (currentTick >= interval) {
            currentTick = 0;
        }
    }

}
