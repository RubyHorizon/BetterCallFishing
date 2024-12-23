package me.shershnyaga.bettercallfishing.config;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.shershnyaga.bettercallfishing.utils.integrations.MythicMobsUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Optional;

@Getter
public class MythicMobsConfig {

    private HashMap<String, MythicMobInfo> mobs = new HashMap<>();

    public MythicMobsConfig(FileConfiguration config) {

        config.getKeys(false).forEach(key -> {
            if (MythicBukkit.inst().getMobManager().getMythicMob(key).isPresent()) {
                float chance = (float) config.getDouble(key + ".catch-chance");
                mobs.put(key, new MythicMobInfo(key, chance));
            } else {
                Bukkit.getLogger().info(ChatColor.RED + "[BetterCallFishing] Mythic mob "
                        + key + " doesn't exist! Please load it to MythicMobs!");
            }
        });

    }

    @AllArgsConstructor
    @Getter
    public static class MythicMobInfo {
        private String id;
        private float spawnChance;

        public Optional<Entity> spawn(Location spawnLocation) {

            if (!MythicMobsUtil.isEnabled()) {
                return Optional.empty();
            }

            MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob(id).orElse(null);

            if (mob == null) {
                return Optional.empty();
            }

            ActiveMob activeMob = mob.spawn(BukkitAdapter.adapt(spawnLocation),1);
            return Optional.of(activeMob.getEntity().getBukkitEntity());
        }
    }
}
