package jp.azisaba.main.survival;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class AzisabaSurvivalConfig {

	private AzisabaSurvival plugin;
	private FileConfiguration conf;

	@ConfigOptions(path = "Chat.Prefix", type = OptionType.CHAT_FORMAT)
	public String chatPrefix = "&c[&6Survival&c] ";

	@ConfigOptions(path = "WitherCanceller.AllowWorlds")
	public List<String> witherAllowWorlds = new ArrayList<>(Arrays.asList("World1", "World2"));
	@ConfigOptions(path = "WitherCanceller.Message.CancelMessage", type = OptionType.CHAT_FORMAT)
	public String cancelWitherSpawnMessage = "&cこのワールドではウィザーをスポーンさせることはできません!";
	@ConfigOptions(path = "WitherCanceller.Message.MessageRadius")
	public double witherCancelMessageRadius = 5;

	@ConfigOptions(path = "HomeLimiter.DefaultHomeLimit")
	public int defaultHomeLimit = 0;

	@ConfigOptions(path = "Seichi.EnabledWorlds")
	public List<String> enabledWorlds = new ArrayList<>(
			Arrays.asList("resource", "resource_nether", "resource_the_end"));
	private HashMap<Material, Double> moneyMap = new HashMap<>();

	@ConfigOptions(path = "Vote.Tickets")
	public int voteTickets = 50;

	@ConfigOptions(path = "MoneyFly.AllowWorlds")
	public List<String> moneyFlyAllowWorldNames = new ArrayList<>(
			Arrays.asList("main", "main_nether", "main_end", "new_flat", "main_flat", "resource", "resource_nether",
					"resource_the_end"));

	private HashMap<String, Integer> homeLimit = new HashMap<>();

	public AzisabaSurvivalConfig(AzisabaSurvival plugin) {
		this.plugin = plugin;
		this.conf = plugin.getConfig();
	}

	public void additional() {

		/**
		 * Home Per World
		 */

		boolean rt = false;
		if (conf.getConfigurationSection("HomeLimiter.Worlds") == null) {
			conf.set("HomeLimiter.Worlds.worldName", 3);
			conf.set("HomeLimiter.Worlds.worldName2", 2);
			rt = true;
		}

		if (rt) {
			plugin.saveConfig();
		} else {
			for (String str : conf.getConfigurationSection("HomeLimiter.Worlds").getKeys(false)) {
				int limit = conf.getInt("HomeLimiter.Worlds." + str, 0);

				homeLimit.put(str, limit);
			}

			this.defaultHomeLimit = conf.getInt("HomeLimiter.DefaultHomeLimit", 0);
		}

		/**
		 * Seichi
		 */

		if (conf.getConfigurationSection("Seichi.MoneyMap") == null) {
			conf.set("Seichi.MoneyMap.DIRT", 10);
			conf.set("Seichi.MoneyMap.GRASS", 10);
			conf.set("Seichi.MoneyMap.STONE", 10);
			plugin.saveConfig();
		} else {
			for (String key : conf.getConfigurationSection("Seichi.MoneyMap").getKeys(false)) {

				Material mat = null;
				try {
					mat = Material.valueOf(key.toUpperCase());
				} catch (Exception e) {
					plugin.getLogger().warning(key + " という名前のブロックが見つかりませんでした。");
					continue;
				}

				if (mat == null) {
					plugin.getLogger().warning(key + " という名前のブロックが見つかりませんでした。");
					continue;
				}

				double value = conf.getDouble("Seichi.MoneyMap." + key);

				moneyMap.put(mat, value);
			}
		}
	}

	public int getHomeLimit(World world) {
		if (!homeLimit.containsKey(world.getName())) {
			return defaultHomeLimit;
		}

		return homeLimit.get(world.getName());
	}

	public double getValueFromMaterial(Material mat) {
		if (!moneyMap.containsKey(mat)) {
			return -1;
		}

		double value = moneyMap.get(mat);

		if (value <= 0) {
			return -1;
		}

		return value;
	}

	public void loadConfig() {
		for (Field field : getClass().getFields()) {
			ConfigOptions anno = field.getAnnotation(ConfigOptions.class);

			if (anno == null) {
				continue;
			}

			String path = anno.path();

			if (conf.get(path) == null) {

				try {

					if (anno.type() == OptionType.NONE) {
						conf.set(path, field.get(this));
					} else if (anno.type() == OptionType.LOCATION) {
						Location loc = (Location) field.get(this);

						conf.set(path, loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ()
								+ "," + loc.getYaw() + "," + loc.getPitch());
					} else if (anno.type() == OptionType.CHAT_FORMAT) {

						String msg = (String) field.get(this);
						conf.set(path, msg);

						msg = ChatColor.translateAlternateColorCodes('&', msg);
						field.set(this, msg);
					} else if (anno.type() == OptionType.SOUND) {
						conf.set(path, field.get(this).toString());
					} else if (anno.type() == OptionType.LOCATION_LIST) {
						@SuppressWarnings("unchecked")
						List<Location> locations = (List<Location>) field.get(this);

						List<String> strs = new ArrayList<String>();

						if (!locations.isEmpty()) {

							for (Location loc : locations) {
								strs.add(loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + ","
										+ loc.getZ()
										+ "," + loc.getYaw() + "," + loc.getPitch());
							}
						} else {
							strs.add("WorldName,X,Y,Z,Yaw,Pitch");
						}

						conf.set(path, strs);
					}

					plugin.saveConfig();
				} catch (Exception e) {
					Bukkit.getLogger().warning("Error: " + e.getMessage());
					e.printStackTrace();
				}
			} else {

				try {
					if (anno.type() == OptionType.NONE) {
						field.set(this, conf.get(path));
					} else if (anno.type() == OptionType.LOCATION) {

						String[] strings = conf.getString(path).split(",");
						Location loc = null;
						try {
							loc = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]),
									Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
							loc.setYaw(Float.parseFloat(strings[4]));
							loc.setPitch(Float.parseFloat(strings[5]));
						} catch (Exception e) {
							// None
						}

						if (loc == null) {
							Bukkit.getLogger().warning("Error. " + path + " の値がロードできませんでした。");
							continue;
						}

						field.set(this, loc);
					} else if (anno.type() == OptionType.SOUND) {

						String name = conf.getString(path);
						Sound sound;

						try {
							sound = Sound.valueOf(name.toUpperCase());
						} catch (Exception e) {
							Bukkit.getLogger().warning("Error. " + path + " の値がロードできませんでした。");
							continue;
						}

						field.set(this, sound);
					} else if (anno.type() == OptionType.CHAT_FORMAT) {

						String unformatMessage = conf.getString(path);

						unformatMessage = ChatColor.translateAlternateColorCodes('&', unformatMessage);

						field.set(this, unformatMessage);
					} else if (anno.type() == OptionType.LOCATION_LIST) {

						List<String> strList = conf.getStringList(path);

						List<Location> locList = new ArrayList<Location>();

						for (String str : strList) {

							String[] strings = str.split(",");
							Location loc = null;
							try {
								loc = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]),
										Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
								loc.setYaw(Float.parseFloat(strings[4]));
								loc.setPitch(Float.parseFloat(strings[5]));
							} catch (Exception e) {
								// None
							}

							if (loc == null) {
								Bukkit.getLogger().warning("Error. " + path + " の " + str + "がロードできませんでした。");
								continue;
							}

							locList.add(loc);
						}

						field.set(this, locList);
					}
				} catch (Exception e) {
					Bukkit.getLogger().warning("Error. " + e.getMessage());
				}
			}
		}

		additional();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface ConfigOptions {
		public String path();

		public OptionType type() default OptionType.NONE;
	}

	public enum OptionType {
		LOCATION, LOCATION_LIST, SOUND, CHAT_FORMAT, NONE
	}
}
