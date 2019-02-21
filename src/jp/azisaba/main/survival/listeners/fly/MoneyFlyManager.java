package jp.azisaba.main.survival.listeners.fly;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import jp.azisaba.main.survival.AzisabaSurvival;

public class MoneyFlyManager {

	private static HashMap<UUID, Long> expire = new HashMap<UUID, Long>();

	private static AzisabaSurvival plugin;

	public static void init(AzisabaSurvival plugin) {
		MoneyFlyManager.plugin = plugin;
	}

	protected static void addTime(Player p, long milli) {
		if (expire.containsKey(p.getUniqueId()) && expire.get(p.getUniqueId()) > System.currentTimeMillis()) {
			expire.put(p.getUniqueId(), expire.get(p.getUniqueId()) + milli);
		} else {
			expire.put(p.getUniqueId(), System.currentTimeMillis() + milli);
		}
	}

	protected static void addOneHour(Player p) {
		//		addTime(p, 1000L * 60L * 60L);
		addTime(p, 1000L * 60L * 60L);
	}

	public static boolean canMoneyFly(Player p) {
		if (!expire.containsKey(p.getUniqueId())) {
			return false;
		}

		if (expire.get(p.getUniqueId()) < System.currentTimeMillis()) {
			expire.remove(p.getUniqueId());
			return false;
		}

		return true;
	}

	public static double getFlyExpireSeconds(Player p) {

		if (!expire.containsKey(p.getUniqueId())) {
			return -1d;
		}

		long finish = expire.get(p.getUniqueId());
		long now = System.currentTimeMillis();

		if (finish < now) {
			return -1d;
		}

		return (((double) finish) - ((double) now)) / 1000;
	}

	protected static void cancelMoneyFly(Player p) {
		if (expire.containsKey(p.getUniqueId())) {
			expire.remove(p.getUniqueId());

			p.setFlying(false);
			p.setAllowFlight(false);

			setNoGroundDamageTarget(p, true);
		} else {
			return;
		}
	}

	private static List<Player> noDamageTargets = new ArrayList<>();

	protected static void setNoGroundDamageTarget(Player p, boolean b) {

		if (b) {
			if (!noDamageTargets.contains(p)) {
				noDamageTargets.add(p);
			}
		} else {
			if (noDamageTargets.contains(p)) {
				noDamageTargets.remove(p);
			}
		}
	}

	public static void clearBossBars() {
		FlyBossBarTask.stopTask();
		FlyBossBarTask.clearBossBars();
	}

	public static boolean isNoGroundDamageTarget(Player p) {
		return noDamageTargets.contains(p);
	}

	public static List<UUID> getEnableFlyPlayerUUIDs() {
		return new ArrayList<UUID>(expire.keySet());
	}

	protected static String getFormattedSignLine1() {
		return ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "[Money Fly]";
	}

	public static void loadData() {
		File file = new File(plugin.getDataFolder(), "FlyData.yml");

		if (!file.exists()) {
			return;
		}

		YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
		ConfigurationSection sec = conf.getConfigurationSection("Players");

		if (sec == null) {
			return;
		}

		for (String key : sec.getKeys(false)) {
			UUID uuid;

			try {
				uuid = UUID.fromString(key);
			} catch (Exception e) {
				continue;
			}

			long finish = conf.getLong("Players." + key.toString(), System.currentTimeMillis());

			if (finish <= System.currentTimeMillis()) {
				conf.set("Players." + key, null);
				try {
					conf.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				continue;
			}

			expire.put(uuid, finish);

			Player p = Bukkit.getPlayer(uuid);
			if (p != null) {
				p.setAllowFlight(true);
			}
		}
	}

	public static boolean saveData() {

		File file = new File(plugin.getDataFolder(), "FlyData.yml");
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

		for (UUID uuid : expire.keySet()) {
			conf.set("Players." + uuid.toString(), expire.get(uuid));
		}

		try {
			conf.save(file);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
