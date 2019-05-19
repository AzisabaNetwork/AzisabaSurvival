package jp.azisaba.main.survival.listeners.fly;

import java.math.BigInteger;
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

import jp.azisaba.main.homos.database.PlayerDataManager;
import jp.azisaba.main.homos.database.TicketManager;
import jp.azisaba.main.survival.AzisabaSurvival;
import net.md_5.bungee.api.ChatColor;

public class BuyFlyListener implements Listener {

	private AzisabaSurvival plugin;

	public BuyFlyListener(AzisabaSurvival plugin) {
		this.plugin = plugin;
	}

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

		final int value;
		try {
			value = Integer.parseInt(ChatColor.stripColor(line2).replace("チケット", ""));
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

		new Thread() {
			public void run() {

				BigInteger tickets = PlayerDataManager.getPlayerData(p).getTickets();

				if (tickets.compareTo(BigInteger.valueOf(value)) < 0) {
					p.sendMessage(ChatColor.RED + "十分なチケットがありません。");
					return;
				}

				boolean success = TicketManager.removeTicket(p, BigInteger.valueOf(value));

				if (!success) {
					p.sendMessage(ChatColor.RED + "購入に失敗しました。");
					return;
				}

				MoneyFlyManager.addTenMinutes(p);

				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 1);
				p.sendMessage(ChatColor.YELLOW + "10分" + ChatColor.GREEN + "のFlyを購入しました！ (" + value + "チケット)");

				Location loc = b.getLocation();
				Bukkit.getLogger().info("[AS:MoneyFly] " + p.getName() + "がFlyを購入しました。 (" + loc.getBlockX() + ","
						+ loc.getBlockY() + "," + loc.getBlockZ() + ")");

				if (!p.getAllowFlight()) {
					allowFlight(p);
				}
			}
		}.start();
	}

	private void allowFlight(Player p) {
		new BukkitRunnable() {
			public void run() {
				p.setAllowFlight(true);
			}
		}.runTaskLater(plugin, 0);
	}
}
