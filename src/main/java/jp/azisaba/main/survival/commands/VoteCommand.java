package jp.azisaba.main.survival.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.rayzr522.jsonmessage.JSONMessage;
import net.md_5.bungee.api.ChatColor;

public class VoteCommand implements CommandExecutor {

	private final String VOTE_URL = "https://minecraft.jp/servers/azisaba.net";

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof Player) {

			Player p = (Player) sender;

			JSONMessage msg = JSONMessage.create();

			msg.bar(24).newline();
			msg.then(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "ここ" + ChatColor.RESET).openURL(VOTE_URL)
					.then(ChatColor.GRAY + "をクリックして投票しましょう！").newline();
			msg.bar(24);

			msg.send(p);

			p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
			return true;
		}

		sender.sendMessage(ChatColor.RED + "運営は投票忘れたら処刑ってそれ一番言われてるから\n   ---> " + ChatColor.YELLOW + VOTE_URL);
		return true;
	}
}
