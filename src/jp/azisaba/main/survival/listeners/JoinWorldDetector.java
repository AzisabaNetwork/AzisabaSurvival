package jp.azisaba.main.survival.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import jp.azisaba.main.survival.AzisabaSurvival;

public class JoinWorldDetector implements Listener {

	private AzisabaSurvival plugin;

	public JoinWorldDetector(AzisabaSurvival plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onJoinWorld(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		World main = Bukkit.getWorld("main");

		if (main == null) {
			plugin.getLogger().warning("World 'main' not exist.");
			return;
		}

		if (p.getLocation().getWorld().getName().equals("world")) {
			p.teleport(main.getSpawnLocation());
		}
	}
}
