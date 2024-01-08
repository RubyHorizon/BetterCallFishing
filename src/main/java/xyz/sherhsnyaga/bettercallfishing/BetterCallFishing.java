package xyz.sherhsnyaga.bettercallfishing;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.sherhsnyaga.bettercallfishing.metrics.Metrics;

public final class BetterCallFishing extends JavaPlugin {
    private Metrics metrics;
    @Override
    public void onEnable() {
        metrics = new Metrics(this, 20687);
        getServer().getPluginManager().registerEvents(new OnFish(), this);
    }
}
