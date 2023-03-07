package xyz.sherhsnyaga.bettercallfishing;

import org.bukkit.plugin.java.JavaPlugin;

public final class BetterCallFishing extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new onFish(), this);
    }
}
