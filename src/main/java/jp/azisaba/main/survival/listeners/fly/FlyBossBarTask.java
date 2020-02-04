package jp.azisaba.main.survival.listeners.fly;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import jp.azisaba.main.survival.AzisabaSurvival;

public class FlyBossBarTask {

    private static AzisabaSurvival plugin;
    private static BukkitTask task;

    public static void init(AzisabaSurvival plugin) {
        FlyBossBarTask.plugin = plugin;
    }

    private static HashMap<UUID, BossBar> bossBarMap = new HashMap<>();

    public static void runTask() {

        if ( task != null ) {
            return;
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {

                List<UUID> uuidList = MoneyFlyManager.getEnableFlyPlayerUUIDs();

                for ( UUID uuid : uuidList ) {

                    Player p = Bukkit.getPlayer(uuid);

                    if ( p == null ) {
                        continue;
                    }

                    BossBar bar;
                    if ( bossBarMap.containsKey(p.getUniqueId()) ) {
                        bar = bossBarMap.get(p.getUniqueId());
                    } else {
                        bar = Bukkit.createBossBar(
                                ChatColor.YELLOW + "MoneyFly 残り" + ChatColor.GREEN + ": " + ChatColor.RED + "更新中...",
                                BarColor.GREEN, BarStyle.SEGMENTED_20, BarFlag.DARKEN_SKY);
                        bar.setProgress(0d);
                    }

                    double seconds = MoneyFlyManager.getFlyExpireSeconds(p);

                    if ( seconds < 0 ) {
                        MoneyFlyManager.cancelMoneyFly(p);
                        bar.removeAll();
                        continue;
                    }

                    if ( !bar.getPlayers().contains(p) ) {
                        bar.addPlayer(p);
                    }

                    double barMem = seconds / (3600 * 3);

                    if ( barMem > 1d ) {
                        barMem = 1d;
                    }

                    bar.setProgress(barMem);
                    bar.setTitle(ChatColor.YELLOW + "MoneyFly 残り" + ChatColor.GREEN + ": " + ChatColor.RED
                            + formatFromSeconds(seconds));

                    if ( !bossBarMap.containsKey(p.getUniqueId()) ) {
                        bossBarMap.put(p.getUniqueId(), bar);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public static void stopTask() {
        if ( task != null ) {
            task.cancel();
        }
    }

    public static void clearBossBars() {
        for ( UUID uuid : bossBarMap.keySet() ) {
            bossBarMap.get(uuid).removeAll();
        }
    }

    private static String formatFromSeconds(double seconds) {

        int hour = 0;
        int minutes = 0;
        double sec = 0;

        while ( seconds >= 3600 ) {
            hour++;
            seconds -= 3600;
        }

        while ( seconds >= 60 ) {
            minutes++;
            seconds -= 60;
        }

        sec = seconds;

        StringBuilder builder = new StringBuilder();

        if ( hour > 0 ) {
            builder.append(hour + "時間");
        }
        if ( minutes > 0 ) {
            builder.append(minutes + "分");
        }
        if ( sec > 0 ) {
            builder.append(String.format("%.2f", sec) + "秒");
        }

        return builder.toString();
    }
}
