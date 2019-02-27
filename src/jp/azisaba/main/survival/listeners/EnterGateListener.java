package jp.azisaba.main.survival.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import jp.azisaba.main.survival.AzisabaSurvival;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.md_5.bungee.api.ChatColor;

public class EnterGateListener implements Listener {

	private AzisabaSurvival plugin;

	private final Location pos1;
	private final Location pos2;

	public EnterGateListener(AzisabaSurvival plugin) {

		this.plugin = plugin;

		this.pos1 = new Location(Bukkit.getWorld("main"), -26.0, 64, -26.0);
		this.pos2 = new Location(Bukkit.getWorld("main"), -24.0, 68, -24.0);
	}

	private List<Player> selectingPos = new ArrayList<>();

	@EventHandler
	public void onEnterGate(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Location loc = p.getLocation().clone();

		if (loc.getWorld() != pos1.getWorld()) {
			return;
		}

		if (selectingPos.contains(p)) {
			return;
		}

		if (pos1.getX() > loc.getX() || pos1.getZ() > loc.getZ() || pos1.getY() > loc.getY()) {
			return;
		}

		if (pos2.getX() < loc.getX() || pos2.getZ() < loc.getZ() || pos2.getY() < loc.getY()) {
			return;
		}

		selectingPos.add(p);

		World world = p.getWorld();
		Location randomLoc = getRandomLocation(world);

		if (randomLoc == null) {
			p.sendMessage(ChatColor.RED + "良い土地が見つかりませんでした。再度試してください。");

			selectingPos.remove(p);
			return;
		}

		new BukkitRunnable() {
			public void run() {
				p.teleport(randomLoc);

				selectingPos.remove(p);
			}
		}.runTaskLater(plugin, 0);
	}

	private Location getRandomLocation(World world) {

		Location loc = null;

		long start = System.currentTimeMillis();
		while (loc == null || isProtect(loc) || !isSafeLocation(loc)) {

			if (start + 50 < System.currentTimeMillis()) {
				return null;
			}

			loc = getCorrectLocation(generateRandomLocation(world));
		}

		return loc;
	}

	private boolean isProtect(Location loc) {

		WorldGuardPlugin wgPL = ((WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard"));

		if (wgPL != null) {

			WorldGuard wg = WorldGuard.getInstance();

			com.sk89q.worldedit.world.World w = wg.getPlatform().getWorldByName(loc.getWorld().getName());
			Collection<ProtectedRegion> regions = wg.getPlatform().getRegionContainer().get(w).getRegions().values();

			for (ProtectedRegion rg : regions) {

				if (rg.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
					return true;
				}
			}
		}

		GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");

		if (gp != null) {
			Collection<Claim> claims = gp.dataStore.getClaims(loc.getChunk().getX(), loc.getChunk().getZ());

			for (Claim c : claims) {
				if (c.contains(loc, true, false)) {
					return true;
				}
			}
		}

		return false;
	}

	private Location generateRandomLocation(World world) {

		double size = world.getWorldBorder().getSize();

		Random rand = new Random();
		int x = rand.nextInt((int) size) - (int) ((size / 2));
		int z = rand.nextInt((int) size) - (int) ((size / 2));

		return new Location(world, x, 63, z);

	}

	private Location getCorrectLocation(Location loc) {

		loc.setY(257);

		boolean beforeLiquid = false;
		while (!isCorrect(loc)) {
			loc.subtract(0, 1, 0);

			if (beforeLiquid && isLiquid(loc)) {
				return null;
			}

			if (isLiquid(loc)) {
				beforeLiquid = true;
			}

			if (loc.getY() < 0) {
				return null;
			}
		}

		loc.add(0.5, 1, 0.5);

		return loc;
	}

	private final List<Material> filter = Arrays.asList(Material.AIR, Material.VOID_AIR, Material.WATER, Material.LAVA);

	private boolean isCorrect(Location loc) {
		if (filter.contains(loc.getBlock().getType())) {
			return false;
		}

		return true;
	}

	private boolean isLiquid(Location loc) {
		return Arrays.asList(Material.WATER, Material.LAVA).contains(loc.getBlock().getType());
	}

	private boolean isSafeLocation(Location location) {

		Location loc = location.clone();

		if (loc.getBlock().getType().isOccluding()
				|| Arrays.asList(Material.LAVA, Material.WATER).contains(loc.getBlock().getType())) {
			return false;
		}

		loc.add(0, 1, 0);

		if (loc.getBlock().getType().isOccluding()
				|| Arrays.asList(Material.LAVA, Material.WATER).contains(loc.getBlock().getType())) {
			return false;
		}

		return true;
	}
}
