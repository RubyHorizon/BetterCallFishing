package me.shershnyaga.bettercallfishing.utils;

import org.bukkit.Bukkit;

public final class ItemsAdderUtil {
    public static boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
    }
}