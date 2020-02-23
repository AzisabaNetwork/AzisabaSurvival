package jp.azisaba.main.survival.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

import lombok.RequiredArgsConstructor;

import jp.azisaba.main.survival.AzisabaSurvival;
import me.rayzr522.jsonmessage.JSONMessage;

@RequiredArgsConstructor
public class VoteCommand implements CommandExecutor {

    private final AzisabaSurvival plugin;

    private final String VOTE_URL = "https://minecraft.jp/servers/azisaba.net";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if ( sender instanceof Player ) {

            Player p = (Player) sender;

            if ( args.length > 0 && args[0].equalsIgnoreCase("voteitem") && p.hasPermission("azisabasurvival.command.vote.getitem") ) {
                ItemStack item = plugin.getVoteRewardPaper().clone();
                String name = item.getItemMeta().getDisplayName();
                p.getInventory().addItem(item);
                p.sendMessage(name + ChatColor.GRAY + "を付与しました。");
                return true;
            }

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
