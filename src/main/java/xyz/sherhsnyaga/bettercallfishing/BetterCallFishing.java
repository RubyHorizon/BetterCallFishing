package xyz.sherhsnyaga.bettercallfishing;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.sherhsnyaga.bettercallfishing.commands.BetterCallFishCmd;
import xyz.sherhsnyaga.bettercallfishing.config.BarrelConfig;
import xyz.sherhsnyaga.bettercallfishing.events.OnFishEvent;
import xyz.sherhsnyaga.bettercallfishing.metrics.Metrics;

import java.util.Objects;

public final class BetterCallFishing extends JavaPlugin {
    private Metrics metrics;
    private BarrelConfig barrelConfig;
    @Override
    public void onEnable() {
        metrics = new Metrics(this, 20687);
        saveDefaultConfig();
        reloadConfig();

        Objects.requireNonNull(getServer().getPluginCommand("bettercallfishing"))
                .setExecutor(new BetterCallFishCmd(barrelConfig));
        getServer().getPluginManager().registerEvents(new OnFishEvent(barrelConfig), this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        barrelConfig = new BarrelConfig(getConfig());
    }
}
