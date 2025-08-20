package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.google.common.base.Strings;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.utilities.MoreCollectionUtils;

public final class AutoRespawn extends BasicHack {
    private final Map<Player, RespawnTimer> respawnTimers = new ConcurrentHashMap<>();

    @AutoLoad
    private long respawnDelay;

    @AutoLoad
    private long loginRespawnDelay;

    @AutoLoad
    private List<String> respawnQuotes;

    public AutoRespawn(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.respawnDelay = Math.max(0L, this.respawnDelay);
        this.loginRespawnDelay = Math.max(0L, this.loginRespawnDelay);
        if (this.respawnQuotes == null) {
            this.respawnQuotes = new ArrayList<>();
        }
    }

    @Override
    public void onDisable() {
        this.respawnTimers.forEach((player, timer) -> timer.stop());
        this.respawnTimers.clear();
        super.onDisable();
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        if (this.respawnDelay <= 0) {
            this.logger.info("Player [" + player.getName() + "] died, respawning.");
            // This is necessary as respawning the player IMMEDIATELY means also not allowing the
            // death process to occur (such as dropping items) to occur prior to the respawn.
            player.getScheduler().runDelayed(this.plugin, (task) -> autoRespawnPlayer(player), null, 1L);
        } else {
            this.logger.info("Player [" + player.getName() + "] died, setting respawn timer: " + this.respawnDelay);
            this.respawnTimers.computeIfPresent(player, (_player, timer) -> timer.stop());
            this.respawnTimers.put(player, new RespawnTimer(player, this.respawnDelay, this::autoRespawnPlayer));
        }
    }

    @EventHandler
    public void onPlayerLogin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!player.isDead()) {
            return;
        }
        if (this.loginRespawnDelay <= 0) {
            this.logger.info("Player [" + player.getName() + "] logged in while dead, respawning.");
            // This is necessary as respawning the player IMMEDIATELY means also not allowing the
            // death process to occur (such as dropping items) to occur prior to the respawn.
            player.getScheduler().runDelayed(this.plugin, (task) -> autoRespawnPlayer(player), null, 1L);
        } else {
            this.logger.info("Player [" + player.getName() + "] logged in while dead, setting respawn timer: " + this.loginRespawnDelay);
            this.respawnTimers.computeIfPresent(player, (_player, timer) -> timer.stop());
            this.respawnTimers.put(player, new RespawnTimer(player, this.loginRespawnDelay, this::autoRespawnPlayer));
        }
    }

    @EventHandler
    public void onPlayerLogout(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (player.isDead()) {
            this.respawnTimers.computeIfPresent(player, (_player, timer) -> timer.stop());
            this.logger.info("Player [" + player.getName() + "] logged out while dead.");
        }
    }

    private void autoRespawnPlayer(final Player player) {
        player.spigot().respawn();
        final String message = MoreCollectionUtils.randomElement(this.respawnQuotes, ThreadLocalRandom.current());
        if (Strings.isNullOrEmpty(message)) {
            return;
        }
        player.sendMessage(ChatUtils.parseColor(message));
    }

    // ------------------------------------------------------------
    // Respawn Timer
    // ------------------------------------------------------------

    private final class RespawnTimer {
        private final Consumer<Player> handler;
        private BossBar bar;
        private long previousTime;
        private final long setTime;
        private long timeRemaining;
        private long secondTimer;
        private ScheduledTask processor;

        RespawnTimer(final Player player, final long delay, final Consumer<Player> handler) {
            this.handler = handler;
            this.previousTime = System.currentTimeMillis();
            this.setTime = this.timeRemaining = delay;
            this.secondTimer = 1000L;
            this.bar = Bukkit.createBossBar(generateBarTitle(), BarColor.WHITE, BarStyle.SOLID);
            this.bar.setVisible(true);
            this.bar.setProgress(1.0d);
            this.bar.addPlayer(player);
            this.processor = player.getScheduler().runAtFixedRate(AutoRespawn.this.plugin(), (task) -> {
                if (tick(player)) {
                    if (!AutoRespawn.this.respawnTimers.remove(player, this)) {
                        AutoRespawn.this.logger.warning("Could not remove respawn timer for player [" + player.getName() + "] as it doesn't match this timer!");
                    }
                    stop(); // Do this just in-case stop()'s processor-cancel method, which cancels THIS task, would prevent the removal above.
                }
            }, null, 1L, 1L);
        }

        private String generateBarTitle() {
            if (this.timeRemaining <= 1_000L) {
                return "Respawning now.";
            }
            if (this.timeRemaining < 60_000L) {
                return "Respawning in " + (int) Math.ceil(this.timeRemaining / 1_000d) + " seconds.";
            }
            if (this.timeRemaining < 120_000L) {
                return "Respawning in 1 minute.";
            }
            if (this.timeRemaining < 3_600_000L) {
                return "Respawning in " + (int) Math.ceil(this.timeRemaining / 60_000d) + " minutes.";
            }
            return "Respawning in " + (int) Math.ceil(this.timeRemaining / 3_600_000d) + " hours.";
        }

        /// @return Returns true if the timer is no longer necessary (player is alive)
        private boolean tick(
            final @NotNull Player player
        ) {
            if (player.isDead()) {
                long currentTime = System.currentTimeMillis();
                long timeDifference = currentTime - this.previousTime;
                this.previousTime = currentTime;
                this.timeRemaining -= timeDifference;
                this.secondTimer -= timeDifference;
                if (this.secondTimer > 0) {
                    return false;
                }
                this.secondTimer = 1000L;
                this.bar.setTitle(generateBarTitle());
                this.bar.setProgress(Math.max(this.timeRemaining / (double) this.setTime, 0));
                if (this.timeRemaining > 0) {
                    return false;
                }
                this.handler.accept(player);
            }
            return true;
        }

        public RespawnTimer stop() {
            if (this.bar != null) {
                this.bar.setVisible(false);
                this.bar.removeAll();
                this.bar = null;
            }
            if (this.processor != null) {
                this.processor.cancel();
                this.processor = null;
            }
            return null;
        }
    }
}
