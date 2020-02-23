package jp.azisaba.main.survival;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

import lombok.Getter;

import jp.azisaba.main.survival.commands.AzisabaSurvivalCommand;
import jp.azisaba.main.survival.commands.VoteCommand;
import jp.azisaba.main.survival.listeners.FlyKickTeleportListener;
import jp.azisaba.main.survival.listeners.HomeCreateCancelListener;
import jp.azisaba.main.survival.listeners.JoinWorldDetector;
import jp.azisaba.main.survival.listeners.MainLoopPreventListener;
import jp.azisaba.main.survival.listeners.RandomTeleportGateListener;
import jp.azisaba.main.survival.listeners.VoteListener;
import jp.azisaba.main.survival.listeners.WitherCancelListener;
import jp.azisaba.main.survival.listeners.fly.BuyFlyListener;
import jp.azisaba.main.survival.listeners.fly.FlyBossBarTask;
import jp.azisaba.main.survival.listeners.fly.FlySignCreateListener;
import jp.azisaba.main.survival.listeners.fly.FlyUpdateListener;
import jp.azisaba.main.survival.listeners.fly.MoneyFlyManager;
import jp.azisaba.main.survival.listeners.fly.MoneyFlyParticleTask;

public class AzisabaSurvival extends JavaPlugin {

    private static AzisabaSurvivalConfig config;
    @Getter
    private static Economy economy = null;

    @Getter
    private YamlConfiguration voteConfig = null;

    @Getter
    private ItemStack voteRewardPaper;

    @Override
    public void onEnable() {

        voteRewardPaper = new ItemStack(Material.PAPER);
        ItemMeta meta = voteRewardPaper.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(ChatColor.GOLD + "金券");
        meta.setLore(Arrays.asList(ChatColor.GREEN + "換金できます！"));
        voteRewardPaper.setItemMeta(meta);

        voteConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "votes.yml"));

        AzisabaSurvival.config = new AzisabaSurvivalConfig(this);
        AzisabaSurvival.config.loadConfig();

        Bukkit.getPluginManager().registerEvents(new WitherCancelListener(), this);
        Bukkit.getPluginManager().registerEvents(new HomeCreateCancelListener(), this);
        Bukkit.getPluginManager().registerEvents(new VoteListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MainLoopPreventListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RandomTeleportGateListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinWorldDetector(this), this);
        Bukkit.getPluginManager().registerEvents(new FlyKickTeleportListener(this), this);

        Bukkit.getPluginManager().registerEvents(new FlySignCreateListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BuyFlyListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FlyUpdateListener(this), this);

        Bukkit.getPluginCommand("azisabasurvival").setExecutor(new AzisabaSurvivalCommand(this));
        Bukkit.getPluginCommand("azisabasurvival")
                .setPermissionMessage(config.chatPrefix + ChatColor.RED + "あなたにはこのコマンドを実行する権限がありません！");
        Bukkit.getPluginCommand("vote").setExecutor(new VoteCommand());
        Bukkit.getPluginCommand("vote")
                .setPermissionMessage(config.chatPrefix + ChatColor.RED + "コマンドを実行する権限がないようです... バグ報告に投げてください。");

        FlyBossBarTask.init(this);
        FlyBossBarTask.runTask();

        MoneyFlyManager.init(this);
        MoneyFlyManager.loadData();

        if ( !setupEconomy() ) {
            getLogger().severe("Vault と連携できませんでした。お金追加機能を無効化します。");
        } else {
            getLogger().info("Vault と連携しました。");
        }

        MoneyFlyParticleTask.runTask(this);

        Bukkit.getLogger().info(getName() + " enabled.");
    }

    @Override
    public void onDisable() {

        try {
            voteConfig.save(new File(getDataFolder(), "votes.yml"));
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        MoneyFlyManager.clearBossBars();
        MoneyFlyManager.saveData();

        if ( Bukkit.getOnlinePlayers().size() > 0 ) {
            for ( Player p : Bukkit.getOnlinePlayers() ) {

                if ( p.getScoreboard() == null ) {
                    continue;
                }

                if ( p.getScoreboard().getObjective("seichi") != null ) {
                    p.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
                }
            }
        }

        MoneyFlyParticleTask.stopTask();
        MoneyFlyManager.writeStoppedData();

        Bukkit.getLogger().info(getName() + " disabled.");
    }

    public void reloadSurvivalConfig() {

        reloadConfig();

        AzisabaSurvival.config = new AzisabaSurvivalConfig(this);
        AzisabaSurvival.config.loadConfig();

        Bukkit.getPluginCommand("azisabasurvival")
                .setPermissionMessage(config.chatPrefix + ChatColor.RED + "あなたにはこのコマンドを実行する権限がありません！");
    }

    public static AzisabaSurvivalConfig getSurvivalConfig() {
        return config;
    }

    public static boolean setupEconomy() {
        try {
            if ( Bukkit.getPluginManager().getPlugin("Vault") == null ) {
                return false;
            }
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if ( rsp == null ) {
                return false;
            }
            economy = rsp.getProvider();
            return economy != null;
        } catch ( Exception e ) {
            return false;
        }
    }
}
