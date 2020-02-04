package jp.azisaba.main.survival.listeners.fly;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import net.md_5.bungee.api.ChatColor;

import jp.azisaba.main.survival.AzisabaSurvival;

public class FlySignCreateListener implements Listener {

    public FlySignCreateListener(AzisabaSurvival plugin) {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignPlace(SignChangeEvent e) {

        if ( e.isCancelled() ) {
            return;
        }

        Block b = e.getBlock();
        String line1 = e.getLine(0);
        String line2 = e.getLine(1);

        Player p = e.getPlayer();

        if ( p == null || !line1.equalsIgnoreCase("[moneyfly]") ) {
            return;
        }

        if ( !p.hasPermission("azisabasurvival.moneyfly.create") ) {
            return;
        }

        int value = 500; // チケット

        if ( !line2.equals("") ) {
            try {
                value = Integer.parseInt(line2);

                if ( value < 0 ) {
                    throw new NumberFormatException("minus value");
                }
            } catch ( NumberFormatException ex ) {

                if ( ex.getMessage().equalsIgnoreCase("minus value") ) {
                    p.sendMessage(ChatColor.RED + "正の数を入力してください");
                } else {
                    p.sendMessage(ChatColor.RED + "数字を入力してください");
                }

                b.breakNaturally();
                return;
            }
        }

        e.setLine(0, MoneyFlyManager.getFormattedSignLine1());
        e.setLine(1, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "" + value + "チケット");
        e.setLine(2, "");
        e.setLine(3, ChatColor.RED + "" + ChatColor.BOLD + "右クリックで購入");

        p.sendMessage(ChatColor.GREEN + "Fly購入看板を設定しました。");
    }
}
