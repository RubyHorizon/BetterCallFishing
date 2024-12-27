package me.shershnyaga.bettercallfishing.config;

import dev.lone.itemsadder.api.CustomStack;
import lombok.*;
import me.shershnyaga.bettercallfishing.hooks.PluginHooks;
import me.shershnyaga.bettercallfishing.hooks.ItemsAdderHook;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Deprecated
public class BarrelConfigOld {
    private final Random random;

    @Getter
    private final List<ItemSettings> itemSettingsList;

    public BarrelConfigOld() {
        random = new Random();
        itemSettingsList = new ArrayList<>();

        // setConfiguration(configuration);
    }

    public List<ItemSettings> parseOldConfig(FileConfiguration config) {
        List<ItemSettings> itemSettingsList = new ArrayList<>();
        Set<String> keys = config.getConfigurationSection("barrel-items").getKeys(false);

        for (String key: keys) {
            int chance = config.getInt("barrel-items." + key + ".chance");
            int minCount = config.getInt("barrel-items." + key + ".min-count");
            int maxCount = config.getInt("barrel-items." + key + ".max-count");

            ItemSettings settings = new ItemSettings(key, chance, minCount, maxCount, 0);
            itemSettingsList.add(settings);
        }

        return itemSettingsList;
    }


    @AllArgsConstructor
    public static class ItemSettings {

        @Getter
        private String id;

        @Getter
        private float chance;

        @Getter
        private int minCount;

        @Getter
        private int maxCount;

        @Setter
        private int counter;

        public Optional<ItemStack> getItem() {
            return getItem(1);
        }

        public Optional<ItemStack> getItem(int amount) {
            if (id.startsWith("IA:")) {
                ItemsAdderHook itemsAdderHook = (ItemsAdderHook) PluginHooks.ITEMS_ADDER.getHook();
                return itemsAdderHook.getItem(id, amount);
            } else if (Material.matchMaterial(id) != null) {
                return Optional.of(new ItemStack(Objects.requireNonNull(Material.getMaterial(id)), amount));
            } else {

                return Optional.empty();
            }
        }

        public boolean isIAItem() {
            return id.startsWith("IA:");
        }

        public boolean isLoadedIAItem() {
            if (isIAItem()) {
                return CustomStack.isInRegistry(id.replace("IA:", ""));
            }

            return false;
        }
    }
}
