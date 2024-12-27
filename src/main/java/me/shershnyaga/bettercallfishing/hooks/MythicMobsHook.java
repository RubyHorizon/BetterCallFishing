package me.shershnyaga.bettercallfishing.hooks;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;


@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class MythicMobsHook implements PluginHook {

    @Override
    public String getHookName() {
        return "MythicMobs";
    }

    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
    }
}
