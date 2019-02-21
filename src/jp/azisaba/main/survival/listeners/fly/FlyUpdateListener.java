package jp.azisaba.main.survival.listeners.fly;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import jp.azisaba.main.survival.AzisabaSurvival;

public class FlyUpdateListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		if (MoneyFlyManager.canMoneyFly(p)
				&& AzisabaSurvival.getSurvivalConfig().moneyFlyAllowWorldNames.contains(p.getWorld().getName())) {
			p.setAllowFlight(true);

			if (!p.isOnGround()) {
				p.setFlying(true);
			}
		}
	}

	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent e) {
		Player p = e.getPlayer();
		World world = p.getWorld();

		if (!MoneyFlyManager.canMoneyFly(p)) {
			return;
		}

		if (AzisabaSurvival.getSurvivalConfig().moneyFlyAllowWorldNames.contains(world.getName())) {
			p.setAllowFlight(true);
		} else if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
			p.setAllowFlight(false);
		}
	}
}
