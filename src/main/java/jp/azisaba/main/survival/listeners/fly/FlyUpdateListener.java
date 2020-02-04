package jp.azisaba.main.survival.listeners.fly;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import jp.azisaba.main.survival.AzisabaSurvival;

public class FlyUpdateListener implements Listener {

    private final AzisabaSurvival plugin;

    public FlyUpdateListener(AzisabaSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if ( MoneyFlyManager.canMoneyFly(p)
                && AzisabaSurvival.getSurvivalConfig().moneyFlyAllowWorldNames.contains(p.getWorld().getName()) ) {
            p.setAllowFlight(true);

            if ( !p.isOnGround() ) {
                p.setFlying(true);
            }
        }
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        World world = p.getWorld();

        if ( !MoneyFlyManager.canMoneyFly(p) ) {
            return;
        }

        if ( AzisabaSurvival.getSurvivalConfig().moneyFlyAllowWorldNames.contains(world.getName()) ) {
            p.setAllowFlight(true);
        } else if ( p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR ) {
            p.setAllowFlight(false);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if ( !(e.getEntity() instanceof Player) ) {
            return;
        }

        Player p = (Player) e.getEntity();

        if ( e.getCause() != DamageCause.FALL ) {
            return;
        }

        if ( MoneyFlyManager.isNoGroundDamageTarget(p) ) {
            e.setCancelled(true);
            MoneyFlyManager.setNoGroundDamageTarget(p, false);
            return;
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if ( !p.isOnGround() ) {
            return;
        }

        if ( MoneyFlyManager.isNoGroundDamageTarget(p) ) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    MoneyFlyManager.setNoGroundDamageTarget(p, false);
                }
            }.runTaskLater(plugin, 1);
        }
    }
}
