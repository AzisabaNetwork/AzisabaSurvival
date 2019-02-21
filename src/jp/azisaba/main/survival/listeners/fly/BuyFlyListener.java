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

import jp.azisaba.main.survival.AzisabaSurvival;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class BuyFlyListener implements Listener {

	private HashMap<Player, Long> lastBought = new HashMap<>();

	@EventHandler
	public void onClickSign(PlayerInteractEvent e) {
		Player p = e.getPlayer();

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Block b = e.getClickedBlock();

		if (b.getType() != Material.SIGN && b.getType() != Material.WALL_SIGN) {
			return;
		}

		Sign s = (Sign) b.getState();
		String line1 = s.getLine(0);
		String line2 = s.getLine(1);

		if (!line1.equals(MoneyFlyManager.getFormattedSignLine1())) {
			return;
		}

		double value = -1;
		try {
			value = Double.parseDouble(ChatColor.stripColor(line2).replace("円", ""));
		} catch (Exception ex) {
			return;
		}

		if (value < 0) {
			Location loc = b.getLocation();
			Bukkit.getLogger().warning("[AzisabaSurvival:MoneyFly] Value cannot be minus value. (" + loc.getBlockX()
					+ "," + loc.getBlockY() + "," + loc.getBlockZ() + ")");
			return;
		}

		if (!p.hasPermission("azisabasurvival.moneyfly.buy")) {
			p.sendMessage(ChatColor.RED + "購入する権限がありません！");
			return;
		}

		if (lastBought.containsKey(p) && lastBought.get(p) + (1000 * 1) > System.currentTimeMillis()) {
			return;
		}

		lastBought.put(p, System.currentTimeMillis());

		Economy econ = AzisabaSurvival.getEconomy();

		if (econ == null) {
			p.sendMessage(ChatColor.RED + "購入に失敗しました。#1");
			return;
		}

		if (!econ.has(p, value)) {
			p.sendMessage(ChatColor.RED + "十分なお金がありません。");
			return;
		}

		EconomyResponse r = econ.withdrawPlayer(p, value);

		if (!r.transactionSuccess()) {
			p.sendMessage(ChatColor.RED + "購入に失敗しました。#2");
			return;
		}

		MoneyFlyManager.addTenMinutes(p);

		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 1);
		p.sendMessage(ChatColor.YELLOW + "10分" + ChatColor.GREEN + "のFlyを購入しました！ (" + value + "円)");

		Location loc = b.getLocation();
		Bukkit.getLogger().info("[AS:MoneyFly] " + p.getName() + "がFlyを購入しました。 (" + loc.getBlockX() + ","
				+ loc.getBlockY() + "," + loc.getBlockZ() + ")");

		p.setAllowFlight(true);
	}
}
