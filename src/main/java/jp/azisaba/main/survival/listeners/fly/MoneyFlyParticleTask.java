package jp.azisaba.main.survival.listeners.fly;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import jp.azisaba.main.survival.AzisabaSurvival;

public class MoneyFlyParticleTask {

    private static BukkitTask task;

    public static void runTask(AzisabaSurvival plugin) {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                List<UUID> plist = MoneyFlyManager.getEnableFlyPlayerUUIDs();

                if ( plist.size() <= 0 ) {
                    return;
                }

                for ( UUID uuid : plist ) {
                    Player p = Bukkit.getPlayer(uuid);

                    if ( p == null || !p.isFlying()
                            || p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR ) {
                        continue;
                    }

                    List<Player> nearPlayerList = new ArrayList<>();
                    for ( Entity ent : p.getWorld().getNearbyEntities(p.getLocation(), 60, 60, 60) ) {
                        if ( ent instanceof Player && (Player) ent != p ) {
                            nearPlayerList.add((Player) ent);
                        }
                    }

                    if ( nearPlayerList.size() <= 0 ) {
                        return;
                    }

                    Location spawn = p.getLocation().clone();
                    for ( Player near : nearPlayerList ) {
                        near.spawnParticle(Particle.CLOUD, spawn, 4, 0, 0, 0, 0.02);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    public static void stopTask() {
        if ( task != null ) {
            task.cancel();
            task = null;
        }
    }
}
