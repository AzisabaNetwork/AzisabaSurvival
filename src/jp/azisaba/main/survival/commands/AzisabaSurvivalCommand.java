package jp.azisaba.main.survival.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import jp.azisaba.main.survival.AzisabaSurvival;
import net.md_5.bungee.api.ChatColor;

public class AzisabaSurvivalCommand implements CommandExecutor {

	private AzisabaSurvival plugin;

	public AzisabaSurvivalCommand(AzisabaSurvival plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (args.length <= 0) {
			sender.sendMessage(ChatColor.RED + "Usage: " + cmd.getUsage().replace("{LABEL}", label));
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			plugin.reloadSurvivalConfig();

			sender.sendMessage(AzisabaSurvival.getSurvivalConfig().chatPrefix + ChatColor.GREEN + "設定を再読み込みしました！");
			return true;
		}

		if (args[0].equalsIgnoreCase("money")) {

			boolean toggle = !AzisabaSurvival.isEnableEarnMoney();
			AzisabaSurvival.setEnableEarnMoney(toggle);

			sender.sendMessage(AzisabaSurvival.getSurvivalConfig().chatPrefix + ChatColor.YELLOW + "now: " + toggle);
			return true;
		}

		sender.sendMessage(ChatColor.RED + "Usage: " + cmd.getUsage().replace("{LABEL}", label));
		return true;
	}
}
