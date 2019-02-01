package jp.azisaba.main.survival.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import jp.azisaba.main.survival.AzisabaSurvival;

public class MainLoopPreventListener implements Listener {

	private AzisabaSurvival plugin;

	public MainLoopPreventListener(AzisabaSurvival plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Location loc = p.getLocation();

		if (loc.getX() >= -3 && loc.getX() <= 3 && loc.getZ() >= -3 && loc.getZ() <= 3 && loc.getY() >= 58
				&& loc.getY() <= 66) {

			Location spawn = p.getWorld().getSpawnLocation();

			p.teleport(spawn);

			plugin.getLogger().info(p.getName() + " のロビーループを阻止 (中央にTP)");
		}
	}
}
