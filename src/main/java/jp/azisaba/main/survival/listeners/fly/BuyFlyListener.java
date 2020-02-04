package jp.azisaba.main.survival.listeners.fly;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import lombok.RequiredArgsConstructor;

import jp.azisaba.main.survival.AzisabaSurvival;

@RequiredArgsConstructor
public class BuyFlyListener implements Listener {

    private final AzisabaSurvival plugin;

    private final HashMap<Player, Long> lastBought = new HashMap<>();

    @EventHandler
    public void onClickSign(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if ( e.getAction() != Action.RIGHT_CLICK_BLOCK ) {
            return;
        }

        Block b = e.getClickedBlock();

        if ( b.getType() != Material.SIGN && b.getType() != Material.WALL_SIGN ) {
            return;
        }

        Sign s = (Sign) b.getState();
        String line1 = s.getLine(0);
        String line2 = s.getLine(1);

        if ( !line1.equals(MoneyFlyManager.getFormattedSignLine1()) ) {
            return;
        }

        final int value;
        try {
            value = Integer.parseInt(ChatColor.stripColor(line2).replace("円", ""));
        } catch ( Exception ex ) {
            return;
        }

        if ( value < 0 ) {
            Location loc = b.getLocation();
            Bukkit.getLogger().warning("[AzisabaSurvival:MoneyFly] Value cannot be minus value. (" + loc.getBlockX()
                    + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")");
            return;
        }

        if ( !p.hasPermission("azisabasurvival.moneyfly.buy") ) {
            p.sendMessage(ChatColor.RED + "購入する権限がありません！");
            return;
        }

        if ( lastBought.getOrDefault(p, 0L) + 1000 > System.currentTimeMillis() ) {
            return;
        }

        lastBought.put(p, System.currentTimeMillis());

        new Thread() {
            @Override
            public void run() {

                Economy econ = AzisabaSurvival.getEconomy();
                if ( econ == null ) {
                    p.sendMessage(ChatColor.RED + "現在このアイテムを購入することができません。運営に問い合わせてください (エラー: Economy Not Found)");
                    return;
                }

                if ( econ.getBalance(p) < value ) {
                    p.sendMessage(ChatColor.RED + "所持金が足りません！購入するためには" + ChatColor.YELLOW + "" + value + "円" + ChatColor.RED + "必要です！");
                    return;
                }

                EconomyResponse res = econ.withdrawPlayer(p, value);

                if ( !res.transactionSuccess() ) {
                    p.sendMessage(ChatColor.RED + "購入に失敗しました (エラー: " + res.errorMessage + ")");
                    return;
                }

                MoneyFlyManager.addTenMinutes(p);

                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 1);
                p.sendMessage(ChatColor.YELLOW + "10分" + ChatColor.GREEN + "のFlyを購入しました！ (" + value + "円)");

                Location loc = b.getLocation();
                Bukkit.getLogger().info("[AS:MoneyFly] " + p.getName() + "がFlyを購入しました。 (" + loc.getBlockX() + ","
                        + loc.getBlockY() + "," + loc.getBlockZ() + ")");

                if ( !p.getAllowFlight() ) {
                    allowFlight(p);
                }
            }
        }.start();
    }

    private void allowFlight(Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                p.setAllowFlight(true);
            }
        }.runTaskLater(plugin, 0);
    }
}
