package xyz.sherhsnyaga.bettercallfishing.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BarrelConfig {
    private final Random random;
    @Getter
    private boolean isEnable;
    private int catchChance;
    private final List<ItemSettings> itemSettingsList;

    public BarrelConfig(FileConfiguration configuration) {
        random = new Random();
        itemSettingsList = new ArrayList<>();
        setConfiguration(configuration);
    }

    public void setConfiguration(FileConfiguration config) {
        itemSettingsList.clear();
        isEnable = config.getBoolean("enable-barrel-catch");
        catchChance = config.getInt("barrel-catch-chance");

        Set<String> keys = config.getConfigurationSection("barrel-items").getKeys(false);

        for (String key: keys) {
            int chance = config.getInt("barrel-items." + key + ".chance");
            int minCount = config.getInt("barrel-items." + key + ".min-count");
            int maxCount = config.getInt("barrel-items." + key + ".max-count");

            ItemSettings settings = new ItemSettings(Material.getMaterial(key), chance, minCount, maxCount, 0);
            itemSettingsList.add(settings);
        }
    }

    public HashMap<Integer, ItemStack> generateBarrelInventoryMap() {
        HashMap<Integer, ItemStack> inventory = new HashMap<>();

        List<Integer> slotList = new ArrayList<>();

        for (int i=0; i<27; i++)
            slotList.add(i);

        List<ItemSettings> itemSettings = new ArrayList<>();

        for (ItemSettings i: itemSettingsList)
            itemSettings.add(new ItemSettings(i.material, i.chance, i.minCount, i.maxCount, 0));

        while (!slotList.isEmpty()) {
            int index = random.nextInt(slotList.size());
            int slot = slotList.get(index);

            for (ItemSettings itemData: itemSettings) {
                if (itemData.counter >= itemData.maxCount)
                    continue;

                if (itemData.counter == 0) {
                    if (getRandom(0, 100) < itemData.chance) {
                        int itemCount = getRandom(itemData.minCount, itemData.maxCount);
                        inventory.put(slot, new ItemStack(itemData.material, itemCount));
                        itemData.counter = itemData.counter + itemCount;
                    }
                }
                else {
                    if (getRandom(0, 100) < itemData.chance) {
                        int itemCount = getRandom(1, itemData.maxCount - itemData.counter);
                        inventory.put(slot, new ItemStack(itemData.material, itemCount));
                        itemData.counter = itemData.counter + itemCount;
                    }
                }
            }

            slotList.remove(index);
        }

        return inventory;
    }

    public boolean testBarrelCatch() {
        if (!isEnable)
            return false;

        return random.nextInt(100) < catchChance;
    }

    private int getRandom(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    @AllArgsConstructor
    @Getter
    private class ItemSettings {
        private Material material;
        private int chance;
        private int minCount;
        private int maxCount;
        @Setter
        private int counter;
    }
}
