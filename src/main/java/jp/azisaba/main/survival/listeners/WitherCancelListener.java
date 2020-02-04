package jp.azisaba.main.survival.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import jp.azisaba.main.survival.AzisabaSurvival;

public class WitherCancelListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onWitherSpawn(CreatureSpawnEvent event) {

        if ( event.getEntityType() != EntityType.WITHER ) {
            return;
        }

        List<String> enableWorlds = AzisabaSurvival.getSurvivalConfig().witherAllowWorlds;

        if ( enableWorlds.contains(event.getLocation().getWorld().getName()) ) {
            return;
        }

        double radius = AzisabaSurvival.getSurvivalConfig().witherCancelMessageRadius;

        List<Player> plist = new ArrayList<>();

        event.getLocation().getWorld()
                .getNearbyEntities(event.getLocation(), radius, radius, radius).stream()
                .filter(ent -> ent instanceof Player).forEach(ent -> plist.add((Player) ent));

        event.setCancelled(true);

        for ( Player p : plist ) {
            p.sendMessage(AzisabaSurvival.getSurvivalConfig().cancelWitherSpawnMessage);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
        }
    }
}
