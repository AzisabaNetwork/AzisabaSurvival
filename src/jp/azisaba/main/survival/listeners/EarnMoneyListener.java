package jp.azisaba.main.survival.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.gmail.nossr50.mcMMO;

import jp.azisaba.main.survival.AzisabaSurvival;
import jp.azisaba.main.survival.util.ScoreboardDisplayer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EarnMoneyListener implements Listener {

	private AzisabaSurvival plugin;

	private HashMap<Player, ScoreboardDisplayer> boardMap = new HashMap<Player, ScoreboardDisplayer>();

	private List<Location> placeLocList = new ArrayList<>();
	private HashMap<Player, List<Location>> breakLocMap = new HashMap<>();

	public EarnMoneyListener(AzisabaSurvival plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent e) {

		if (!AzisabaSurvival.isEnableEarnMoney()) {
			return;
		}

		if (e.isCancelled()) {
			return;
		}

		Player p = e.getPlayer();
		World world = e.getBlock().getWorld();

		if (p.getGameMode() == GameMode.CREATIVE) {
			return;
		}

		if (!AzisabaSurvival.getSurvivalConfig().enabledWorlds.contains(world.getName())) {
			return;
		}

		double value = AzisabaSurvival.getSurvivalConfig().getValueFromMaterial(e.getBlock().getType());

		if (value <= 0) {
			return;
		}

		if (isPlacedByPlayer(e.getBlock())) {
			return;
		}

		if (breakLocMap.containsKey(p) && breakLocMap.get(p).contains(e.getBlock().getLocation())) {
			return;
		}

		if (!p.hasPermission("azisabasurvival.earnmoney")) {
			return;
		}

		boolean success = addMoney(p, value);

		ScoreboardDisplayer disp;
		if (boardMap.containsKey(p)) {
			disp = boardMap.get(p);
		} else {
			disp = new ScoreboardDisplayer(plugin, p);
		}

		if (success) {
			disp.addMoney(value);
		} else {
			disp.addError();
		}
		disp.update();

		if (!boardMap.containsKey(p)) {
			boardMap.put(p, disp);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void addBreakBlock(BlockBreakEvent e) {

		if (!AzisabaSurvival.isEnableEarnMoney()) {
			return;
		}

		if (e.isCancelled()) {
			return;
		}

		Player p = e.getPlayer();

		if (breakLocMap.containsKey(p)) {
			List<Location> locList = breakLocMap.get(p);
			locList.add(0, e.getBlock().getLocation());

			if (locList.size() >= 50) {
				locList.remove(locList.size() - 1);
			}

			breakLocMap.put(p, locList);
		} else {
			breakLocMap.put(p, new ArrayList<Location>(Arrays.asList(e.getBlock().getLocation())));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void addPlaceBlock(BlockPlaceEvent e) {

		if (!AzisabaSurvival.isEnableEarnMoney()) {
			return;
		}

		if (e.isCancelled()) {
			return;
		}

		if (mcMMOEnabled()) {
			return;
		}

		placeLocList.add(0, e.getBlock().getLocation());

		if (placeLocList.size() >= 200) {
			placeLocList.remove(placeLocList.size() - 1);
		}
	}

	private boolean addMoney(Player p, double value) {
		Economy econ = AzisabaSurvival.getEconomy();
		EconomyResponse r = econ.depositPlayer(p, null, value);

		if (!r.transactionSuccess()) {
			plugin.getLogger().warning(p.getName() + "へのお金追加でエラー発生: " + r.errorMessage);
			return false;
		}

		return true;
	}

	private boolean isPlacedByPlayer(Block block) {
		if (mcMMOEnabled()) {
			return mcMMO.getPlaceStore().isTrue(block.getState());
		} else {
			return placeLocList.contains(block.getLocation());
		}
	}

	private boolean mcMMOEnabled() {
		return Bukkit.getPluginManager().getPlugin("mcMMO") != null;
	}
}
