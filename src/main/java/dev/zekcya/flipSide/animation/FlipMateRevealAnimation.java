package dev.zekcya.flipSide.animation;

import dev.zekcya.flipSide.FlipSide;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FlipMateRevealAnimation {

    private final FlipSide plugin;

    public FlipMateRevealAnimation(FlipSide plugin) {
        this.plugin = plugin;
    }

    public void startCountdownAndRevealPlayer() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
        List<String> playerNames = onlinePlayers.stream()
                .filter(player -> !player.getScoreboardTags().contains("Host"))
                .map(Player::getName)
                .collect(Collectors.toList());

        new BukkitRunnable() {
            int countdown = 3;

            @Override
            public void run() {
                if (countdown > 0) {
                    for (Player player : onlinePlayers) {
                        player.sendTitle("§c" + countdown, "", 0, 25, 0);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
                    }
                    countdown--;
                } else {
                    this.cancel();
                    startNameRoller(onlinePlayers, playerNames);
                }
            }
        }.runTaskTimer(this.plugin, 0L, 20L);
    }

    private void startNameRoller(List<Player> onlinePlayers, List<String> playerNames) {
        new BukkitRunnable() {
            int ticks = 0;
            final int totalDurationTicks = 200;
            double lastChangeTime = -1;
            double a = 0.02;
            double b = 0.55;

            @Override
            public void run() {
                if (ticks < totalDurationTicks) {
                    double currentTime = ticks / 20.0;
                    if (currentTime - lastChangeTime >= a * Math.exp(b * currentTime)) {
                        Collections.shuffle(playerNames);
                        for (Player player : onlinePlayers) {

                            if (player.getScoreboardTags().contains("Host")) {
                                continue;
                            }

                            String currentName = playerNames.stream()
                                    .filter(name -> !name.equals(player.getName()))
                                    .findFirst()
                                    .orElse("Herobrine");

                            player.sendTitle(currentName, "Is Your FlipMate", 0, 200, 0);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
                        }
                        lastChangeTime = currentTime;
                    }
                    ticks++;
                } else {
                    this.cancel();
                    revealLinkedPlayers(onlinePlayers);
                }
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }


    private void revealLinkedPlayers(List<Player> onlinePlayers) {
        for (Player player : onlinePlayers) {

            if (player.getScoreboardTags().contains("Host")) {
                continue;
            }

            UUID linkedPlayerId = playerLinker.getLinkedPlayer(player.getUniqueId());
            Player linkedPlayer = Bukkit.getServer().getPlayer(linkedPlayerId);
            if (linkedPlayer != null) {
                player.sendTitle("§c" + linkedPlayer.getName(), "Is Your FlipMate", 0, 40, 20);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::teleportPlayers, 100L);
    }

}
