package me.shershnyaga.bettercallfishing.utils.integrations;

import org.bukkit.Bukkit;

public final class MythicMobsUtil {
    public static boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
    }
}
