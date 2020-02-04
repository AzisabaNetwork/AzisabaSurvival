package jp.azisaba.main.survival.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

import jp.azisaba.main.survival.AzisabaSurvival;

public class FlyKickTeleportListener implements Listener {

    private final AzisabaSurvival plugin;

    private final HashMap<Player, Long> join = new HashMap<>();
    private final HashMap<UUID, Integer> counter = new HashMap<>();

    public FlyKickTeleportListener(AzisabaSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onKickPlayer(PlayerKickEvent e) {
        Player p = e.getPlayer();

        if ( !join.containsKey(p) ) {
            return;
        }

        if ( !e.getReason().equals("Flying is not enabled on this server") ) {
            return;
        }

        if ( join.get(p) + 1000 * 20 > System.currentTimeMillis() ) {
            int count = 0;

            if ( counter.containsKey(p.getUniqueId()) ) {
                count = counter.get(p.getUniqueId());
            }

            count++;
            counter.put(p.getUniqueId(), count);

            if ( count == 3 ) {
                e.setReason(
                        e.getReason() + "\n" + ChatColor.RED + "(次回ログイン時にスポーンにテレポートします)");
            } else {
                e.setReason(
                        e.getReason() + "\n" + ChatColor.RED + "(あと" + (3 - count) + "回ログインに失敗した場合はスポーンにテレポートします)");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        join.put(p, System.currentTimeMillis());

        if ( counter.containsKey(p.getUniqueId()) && counter.get(p.getUniqueId()) >= 3 ) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.teleport(Bukkit.getWorld("main").getSpawnLocation());
                    if ( counter.containsKey(p.getUniqueId()) ) {
                        counter.remove(p.getUniqueId());
                    }
                }
            }.runTaskLater(plugin, 0);
        }
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent e) {
        if ( join.containsKey(e.getPlayer()) ) {
            join.remove(e.getPlayer());
        }
    }
}
