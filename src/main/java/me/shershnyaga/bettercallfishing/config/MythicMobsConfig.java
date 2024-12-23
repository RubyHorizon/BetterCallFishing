package me.shershnyaga.bettercallfishing.config;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.shershnyaga.bettercallfishing.BetterCallFishing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;

import java.util.*;
public class MythicMobsConfig {

    private HashMap<String, MythicMobInfo> mobs = new HashMap<>();

    private Random random = new Random();

    public MythicMobsConfig(FileConfiguration config) {
        for (String key: config.getKeys(false)) {
            float chance = (float) config.getDouble(key + ".catch-chance");
            mobs.put(key, new MythicMobInfo(key, chance));
        }

    }

    public Optional<MythicMobInfo> getRandomMobInfo() {
        List<MythicMobInfo> infos = new ArrayList<>(mobs.values().stream().toList());
        Collections.shuffle(infos);
        for (MythicMobInfo info : infos) {
            if (info.isLoaded()) {
                if (info.spawnChance >= getRandomFloat() && info.spawnChance > 0f) {
                    return Optional.of(info);
                }
            } else {
                BetterCallFishing.log(ChatColor.RED + "Mythic mob \""
                        + info.id + "\" doesn't exist! Please load it to MythicMobs!");
            }
        }

        return Optional.empty();
    }

    public List<MythicMobInfo> getMobs() {
        return new ArrayList<>(mobs.values());
    }

    @AllArgsConstructor
    @Getter
    public static class MythicMobInfo {
        private String id;
        private float spawnChance;

        public Entity spawn(Location spawnLocation) {

            MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob(id).orElse(null);

            ActiveMob activeMob = mob.spawn(BukkitAdapter.adapt(spawnLocation),1);
            return activeMob.getEntity().getBukkitEntity();
        }

        public boolean isLoaded() {
            return MythicBukkit.inst().getMobManager().getMythicMob(id).isPresent();
        }
    }

    private float getRandomFloat() {
        return random.nextFloat() * 100;
    }
}
