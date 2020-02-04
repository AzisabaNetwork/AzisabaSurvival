package jp.azisaba.main.survival.listeners;

import java.io.File;
import java.math.BigInteger;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.google.common.base.Strings;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import net.md_5.bungee.api.ChatColor;

import jp.azisaba.main.homos.classes.PlayerData;
import jp.azisaba.main.homos.database.PlayerDataManager;
import jp.azisaba.main.homos.database.TicketManager;
import jp.azisaba.main.survival.AzisabaSurvival;
import jp.azisaba.main.survival.util.LogWriter;
import me.rayzr522.jsonmessage.JSONMessage;

public class VoteListener implements Listener {

    private final AzisabaSurvival plugin;
    private final String VOTE_URL = "https://minecraft.jp/servers/azisaba.net";

    public VoteListener(AzisabaSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();

        String voter = vote.getUsername();

        try {
            addTickets(voter);
        } catch ( NullPointerException e ) {

            if ( e == null || e.getMessage() == null ) {
                return;
            }

            if ( voter.equals("Votifier Test") || e.getMessage().equals("user not found.") ) {
                return;
            }

            errorTracker(vote, e);
            return;
        } catch ( Exception e ) {

            if ( voter.equals("Votifier Test") ) {
                return;
            }

            errorTracker(vote, e);
            return;
        }

        JSONMessage msg = JSONMessage
                .create(ChatColor.RED + "[" + ChatColor.YELLOW + "投票" + ChatColor.RED + "] " + ChatColor.GREEN
                        + voter + ChatColor.GRAY + "さんがJMSで投票して" + AzisabaSurvival.getSurvivalConfig().voteTickets
                        + "チケットをゲットしました！");

        msg.newline().then(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "ここから投票できます！").openURL(VOTE_URL);

        msg.send(Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        });

        plugin.getLogger().info(voter + " が投票しました。");
    }

    private boolean addTickets(String name) {

        PlayerData data = PlayerDataManager.getPlayerData(name);

        if ( data == null ) {
            throw new NullPointerException("user not found.");
        }

        boolean success = TicketManager.addTicket(data.getUuid(),
                BigInteger.valueOf(AzisabaSurvival.getSurvivalConfig().voteTickets));

        return success;
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
