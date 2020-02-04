package jp.azisaba.main.survival.listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import net.md_5.bungee.api.ChatColor;

import jp.azisaba.main.survival.AzisabaSurvival;

public class HomeCreateCancelListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onReceiveHomeCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        PluginCommand cmd = Bukkit.getPluginCommand("sethome");

        if ( cmd == null ) {
            return;
        }

        String label = e.getMessage().split(" ")[0].substring(1).toLowerCase();

        if ( !cmd.getName().equalsIgnoreCase(label) && !cmd.getAliases().contains(label) ) {
            return;
        }

        String homeName;
        String[] strs = e.getMessage().split(" ");
        if ( strs.length <= 1 ) {
            homeName = getHomeNameIfOne(p);

            if ( homeName == null ) {
                homeName = "home";
            }

        } else if ( strs.length == 2 ) {
            homeName = strs[1];
        } else {
            homeName = strs[2];
        }

        boolean cancel = onRegisterHome(p, homeName);

        if ( cancel ) {
            p.sendMessage(ChatColor.RED + "このワールドに設定できるホーム数を超えてしまいます。 (" + ChatColor.YELLOW + ""
                    + AzisabaSurvival.getSurvivalConfig().getHomeLimit(p.getWorld()) + "個以下" + ChatColor.RED + ")");
            e.setCancelled(true);
        }
    }

    private boolean onRegisterHome(Player p, String homeName) {
        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

        if ( ess == null ) {
            return false;
        }

        User user = ess.getUser(p.getUniqueId());

        try {
            if ( user.getHome(homeName) != null && user.getHome(homeName).getWorld() == p.getWorld() ) {
                return false;
            }
        } catch ( Exception e1 ) {
            // none
        }

        HashMap<World, Integer> homes = new HashMap<>();

        for ( String home : user.getHomes() ) {

            Location loc;

            try {
                loc = user.getHome(home);
            } catch ( Exception e ) {
                continue;
            }

            if ( loc == null || loc.getWorld() == null ) {
                continue;
            }

            if ( homes.containsKey(loc.getWorld()) ) {
                homes.put(loc.getWorld(), homes.get(loc.getWorld()) + 1);
            } else {
                homes.put(loc.getWorld(), 1);
            }
        }

        int current = 0;
        if ( homes.containsKey(p.getWorld()) ) {
            current = homes.get(p.getWorld());
        }

        if ( current >= AzisabaSurvival.getSurvivalConfig().getHomeLimit(p.getWorld()) ) {
            return true;
        }

        return false;
    }

    private String getHomeNameIfOne(Player p) {
        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

        if ( ess == null ) {
            return null;
        }

        User user = ess.getUser(p.getUniqueId());

        if ( user.getHomes().size() == 1 ) {
            return user.getHomes().get(0);
        }

        return null;
    }
}
