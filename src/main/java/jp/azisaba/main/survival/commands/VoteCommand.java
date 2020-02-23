package jp.azisaba.main.survival.commands;

import org.bukkit.Bukkit;
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
        if ( args.length <= 0 || !sender.hasPermission("azisabasurvival.command.vote.give") ) {
            normalExecute(sender);
            return true;
        }

        if ( args[0].equalsIgnoreCase("give") ) {
            Player target = null;
            if ( args.length <= 1 ) {
                if ( !(sender instanceof Player) ) {
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " give <Player>");
                    return true;
                }
                target = (Player) sender;
            } else {
                target = Bukkit.getPlayer(args[1]);
            }

            if ( target == null ) {
                sender.sendMessage(ChatColor.RED + "プレイヤーが見つかりません！");
                return true;
            }

            ItemStack item = plugin.getVoteRewardPaper().clone();
            String name = item.getItemMeta().getDisplayName();
            target.getInventory().addItem(item);
            sender.sendMessage(ChatColor.YELLOW + target.getName() + ChatColor.GRAY + "に" + name + ChatColor.GRAY + "を付与しました。");
        } else {
            normalExecute(sender);
        }
        return true;
    }

    private void normalExecute(CommandSender sender) {
        if ( sender instanceof Player ) {
            Player p = (Player) sender;
            JSONMessage msg = JSONMessage.create();

            msg.bar(24).newline();
            msg.then(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "ここ" + ChatColor.RESET).openURL(VOTE_URL)
                    .then(ChatColor.GRAY + "をクリックして投票しましょう！").newline();
            msg.bar(24);

            msg.send(p);

            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        } else {
            sender.sendMessage(ChatColor.RED + "運営は投票忘れたら処刑ってそれ一番言われてるから\n   ---> " + ChatColor.YELLOW + VOTE_URL);
        }
    }
}
