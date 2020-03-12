package jp.azisaba.main.survival.listeners;

import com.google.common.base.Strings;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import jp.azisaba.main.survival.AzisabaSurvival;
import jp.azisaba.main.survival.util.LogWriter;
import lombok.RequiredArgsConstructor;
import me.rayzr522.jsonmessage.JSONMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;

@RequiredArgsConstructor
public class VoteListener implements Listener {

    private final AzisabaSurvival plugin;
    private final String VOTE_URL = "https://minecraft.jp/servers/azisaba.net";

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();

        String voter = vote.getUsername();

        if (voter.equals("Votifier Test")) {
            return;
        }
        String displayName = plugin.getVoteRewardPaper().getItemMeta().getDisplayName();

        if (Bukkit.getPlayer(voter) != null) {
            Player p = Bukkit.getPlayer(voter);
            p.getInventory().addItem(plugin.getVoteRewardPaper().clone());
            p.sendMessage(ChatColor.GREEN + "投票報酬として " + displayName + ChatColor.GREEN + " を付与しました！");

            JSONMessage msg = JSONMessage.create(ChatColor.RED + "[" + ChatColor.YELLOW + "投票" + ChatColor.RED + "] " + ChatColor.GREEN + voter + ChatColor.GRAY + "さんがJMSで投票して" + displayName + ChatColor.GRAY + "をゲットしました！");
            msg.newline().then(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "ここから投票できます！").openURL(VOTE_URL);
            msg.send(Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));
            return;
        } else {
            try {
                YamlConfiguration conf = plugin.getVoteConfig();
                conf.set(voter, conf.getInt(voter, 0) + 1);
            } catch (Exception e) {
                errorTracker(vote, e);
                return;
            }
        }

        JSONMessage msg = JSONMessage.create(ChatColor.RED + "[" + ChatColor.YELLOW + "投票" + ChatColor.RED + "] " + ChatColor.GREEN + voter + ChatColor.GRAY + "さんがJMSで投票してくれました！");
        msg.newline().then(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "ここから投票できます！").openURL(VOTE_URL);
        msg.send(Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        });

        plugin.getLogger().info(voter + " が投票しました。");
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        YamlConfiguration conf = plugin.getVoteConfig();

        int amount = conf.getInt(p.getName(), 0);
        int amountBackup = amount;

        if (amount > 0) {

            String displayName = plugin.getVoteRewardPaper().getItemMeta().getDisplayName();

            conf.set(p.getName(), null);
            while (amount > 0) {
                int am2 = amount;
                if (am2 > 64) {
                    am2 = 64;
                }

                ItemStack paper = plugin.getVoteRewardPaper().clone();
                paper.setAmount(am2);
                p.getInventory().addItem(paper);

                amount -= am2;
            }

            p.sendMessage(ChatColor.GREEN + "投票報酬として " + displayName + " " + ChatColor.AQUA + amountBackup + "個 " + ChatColor.GREEN + "を付与しました！");

            JSONMessage msg = JSONMessage.create(ChatColor.RED + "[" + ChatColor.YELLOW + "投票" + ChatColor.RED + "] " + ChatColor.GREEN + p.getName() + ChatColor.GRAY + "さんがJMSで投票して" + displayName + ChatColor.GRAY + "をゲットしました！");
            msg.newline().then(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "ここから投票できます！").openURL(VOTE_URL);
            msg.send(Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));
        }
    }

    private void errorTracker(Vote data, Exception e) {

        plugin.getLogger().warning("投票報酬付与でエラー発生。" + "\nName: " + data.getUsername() + "\n" + "詳しくはErrorLog.logを参照");

        LogWriter writer = new LogWriter(new File(plugin.getDataFolder(), "ErrorLog.log"));

        writer.writeLine(Strings.repeat("-", 10) + "[Error]" + Strings.repeat("-", 10) + writer.getEndOfLine()
                + writer.getEndOfLine());
        writer.writeLine("Name: " + data.getUsername() + writer.getEndOfLine() + writer.getEndOfLine());
        writer.writeLine("Service: " + data.getServiceName() + writer.getEndOfLine());
        writer.writeLine(
                "Time: " + data.getTimeStamp() + " (" + data.getLocalTimestamp() + ")" + writer.getEndOfLine()
                        + writer.getEndOfLine());

        writer.writeError(e);

        writer.writeLine(Strings.repeat("-", 25));
    }
}
