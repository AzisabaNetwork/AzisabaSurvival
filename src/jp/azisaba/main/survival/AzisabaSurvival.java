package jp.azisaba.main.survival;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;

import jp.azisaba.main.survival.commands.AzisabaSurvivalCommand;
import jp.azisaba.main.survival.commands.VoteCommand;
import jp.azisaba.main.survival.listeners.EarnMoneyListener;
import jp.azisaba.main.survival.listeners.EnterGateListener;
import jp.azisaba.main.survival.listeners.HomeCreateCancelListener;
import jp.azisaba.main.survival.listeners.JoinWorldDetector;
import jp.azisaba.main.survival.listeners.MainLoopPreventListener;
import jp.azisaba.main.survival.listeners.VoteListener;
import jp.azisaba.main.survival.listeners.WitherCancelListener;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class AzisabaSurvival extends JavaPlugin {

	private static AzisabaSurvivalConfig config;
	private static Economy econ = null;

	private static boolean enableEarnMoney = false;

	@Override
	public void onEnable() {

		AzisabaSurvival.config = new AzisabaSurvivalConfig(this);
		AzisabaSurvival.config.loadConfig();

		if (!setupEconomy()) {

			getLogger().severe("Vault が導入されていません。お金追加機能を無効化します。");
			return;
		} else {

			getLogger().info("Vault と連携しました。お金追加機能を有効化しています...");
			enableEarnMoney = true;
			getLogger().info("完了！");
		}

		Bukkit.getPluginManager().registerEvents(new WitherCancelListener(), this);
		Bukkit.getPluginManager().registerEvents(new HomeCreateCancelListener(), this);
		Bukkit.getPluginManager().registerEvents(new EarnMoneyListener(this), this);
		Bukkit.getPluginManager().registerEvents(new VoteListener(this), this);
		Bukkit.getPluginManager().registerEvents(new MainLoopPreventListener(this), this);
		Bukkit.getPluginManager().registerEvents(new EnterGateListener(this), this);
		Bukkit.getPluginManager().registerEvents(new JoinWorldDetector(this), this);

		Bukkit.getPluginCommand("azisabasurvival").setExecutor(new AzisabaSurvivalCommand(this));
		Bukkit.getPluginCommand("azisabasurvival")
				.setPermissionMessage(config.chatPrefix + ChatColor.RED + "あなたにはこのコマンドを実行する権限がありません！");
		Bukkit.getPluginCommand("vote").setExecutor(new VoteCommand());
		Bukkit.getPluginCommand("vote")
				.setPermissionMessage(config.chatPrefix + ChatColor.RED + "コマンドを実行する権限がないようです... バグ報告に投げてください。");

		Bukkit.getLogger().info(getName() + " enabled.");
	}

	@Override
	public void onDisable() {

		if (Bukkit.getOnlinePlayers().size() > 0) {
			for (Player p : Bukkit.getOnlinePlayers()) {

				if (p.getScoreboard() == null) {
					continue;
				}

				if (p.getScoreboard().getObjective("seichi") != null) {
					p.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
				}
			}
		}

		Bukkit.getLogger().info(getName() + " disabled.");
	}

	public void reloadSurvivalConfig() {

		this.reloadConfig();

		AzisabaSurvival.config = new AzisabaSurvivalConfig(this);
		AzisabaSurvival.config.loadConfig();

		Bukkit.getPluginCommand("azisabasurvival")
				.setPermissionMessage(config.chatPrefix + ChatColor.RED + "あなたにはこのコマンドを実行する権限がありません！");
	}

	public static AzisabaSurvivalConfig getSurvivalConfig() {
		return config;
	}

	public static boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public static Economy getEconomy() {
		if (econ == null) {
			return null;
		}

		return econ;
	}

	public static boolean isEnableEarnMoney() {
		return enableEarnMoney;
	}

	public static void setEnableEarnMoney(boolean enable) {
		enableEarnMoney = enable;
	}
}
