package me.shershnyaga.bettercallfishing.utils.integrations;

import org.bukkit.Bukkit;

public final class MMOItems {
    public static boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("MMOItems") != null;
    }
}
