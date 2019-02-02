package jp.azisaba.main.survival.listeners;

import java.io.File;
import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.earth2me.essentials.Essentials;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import jp.azisaba.main.survival.AzisabaSurvival;
import jp.azisaba.main.survival.util.JSONMessage;
import jp.azisaba.main.survival.util.LogWriter;
import net.ess3.api.MaxMoneyException;
import net.md_5.bungee.api.ChatColor;

public class VoteListener implements Listener {

	private AzisabaSurvival plugin;
	private final String VOTE_URL = "https://minecraft.jp/servers/azisaba.net";

	public VoteListener(AzisabaSurvival plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onVotifierEvent(VotifierEvent event) {
		Vote vote = event.getVote();

		String voter = vote.getUsername();

		try {
			addMoney(voter);
		} catch (Exception e) {

			if (voter.equals("Votifier Test")) {
				return;
			}

			errorTracker(vote, e);
			return;
		}

		JSONMessage msg = JSONMessage
				.create(ChatColor.RED + "[" + ChatColor.YELLOW + "投票" + ChatColor.RED + "] " + ChatColor.GREEN
						+ voter + ChatColor.GRAY + "さんがJMSで投票して" + AzisabaSurvival.getSurvivalConfig().voteMoney
						+ "円をゲットしました！");

		msg.newline().then(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "ここから投票できます！").openURL(VOTE_URL);

		msg.send(Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));

		Bukkit.getOnlinePlayers().forEach(player -> {
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		});

		plugin.getLogger().info(voter + " が投票しました。");
	}

	private void addMoney(String name) throws MaxMoneyException {

		Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

		double value = AzisabaSurvival.getSurvivalConfig().voteMoney;

		ess.getUser(name).giveMoney(BigDecimal.valueOf(value));
	}

	private void errorTracker(Vote data, Exception e) {

		plugin.getLogger().warning("投票報酬付与でエラー発生。" + "\nName: " + data.getUsername() + "\n" + "詳しくはErrorLog.logを参照");

		LogWriter writer = new LogWriter(new File(plugin.getDataFolder(), "ErrorLog.log"));

		writer.writeLine(StringUtils.repeat("-", 10) + "[Error]" + StringUtils.repeat("-", 10) + writer.getEndOfLine()
				+ writer.getEndOfLine());
		writer.writeLine("Name: " + data.getUsername() + writer.getEndOfLine() + writer.getEndOfLine());
		writer.writeLine("Service: " + data.getServiceName() + writer.getEndOfLine());
		writer.writeLine(
				"Time: " + data.getTimeStamp() + " (" + data.getLocalTimestamp() + ")" + writer.getEndOfLine()
						+ writer.getEndOfLine());

		writer.writeError(e);

		writer.writeLine(StringUtils.repeat("-", 25));
	}
}
