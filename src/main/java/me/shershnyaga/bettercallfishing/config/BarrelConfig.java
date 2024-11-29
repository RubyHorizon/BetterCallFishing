package me.shershnyaga.bettercallfishing.config;

import dev.lone.itemsadder.api.CustomStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.shershnyaga.bettercallfishing.BetterCallFishing;
import me.shershnyaga.bettercallfishing.utils.ItemsAdderUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BarrelConfig {
    private final Random random;

    @Getter
    private boolean isEnable;
    private float catchChance;
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

        boolean isIaEnabled = ItemsAdderUtil.isEnabled();

        for (String key: keys) {
            int chance = config.getInt("barrel-items." + key + ".chance");
            int minCount = config.getInt("barrel-items." + key + ".min-count");
            int maxCount = config.getInt("barrel-items." + key + ".max-count");

            ItemSettings settings = null;
            if (key.startsWith("IA:")) {

                String iaKey = key.replace("IA:", "");

                if (!isIaEnabled) {
                    Bukkit.getLogger().info(ChatColor.RED + "[BetterCallFishing] \""
                            + iaKey + "\" is ItemsAdderItem, but This is an ItemsAdder item, but the ItemsAdder plugin " +
                            "is not loaded!!");
                    continue;
                }

                if (!CustomStack.isInRegistry(iaKey)) {
                    settings = new ItemSettings(CustomStack.getInstance(iaKey).getItemStack(),
                            chance, minCount, maxCount, 0);
                } else {
                    Bukkit.getLogger().info(ChatColor.RED + "[BetterCallFishing] \""
                            + iaKey + "\" is not registered in ItemsAdder!");
                }
            } else {
                settings = new ItemSettings(new ItemStack(Objects.requireNonNull(Material.getMaterial(key))),
                        chance, minCount, maxCount, 0);
            }

            if (settings != null) {
                itemSettingsList.add(settings);
            }
        }
    }

    public HashMap<Integer, ItemStack> generateBarrelInventoryMap() {
        HashMap<Integer, ItemStack> inventory = new HashMap<>();

        List<Integer> slotList = new ArrayList<>();

        for (int i=0; i<27; i++) {
            slotList.add(i);
        }

        List<ItemSettings> itemSettings = new ArrayList<>();

        for (ItemSettings i: itemSettingsList) {
            itemSettings.add(new ItemSettings(i.item.clone(), i.chance, i.minCount, i.maxCount, 0));
        }

        while (!slotList.isEmpty()) {
            int index = random.nextInt(slotList.size());
            int slot = slotList.get(index);

            for (ItemSettings itemData: itemSettings) {
                if (itemData.counter >= itemData.maxCount)
                    continue;

                if (itemData.counter == 0) {
                    if (getRandom(0f, 100f) < itemData.chance) {
                        int itemCount = getRandom(itemData.minCount, itemData.maxCount);

                        ItemStack stack = itemData.item.clone();
                        stack.setAmount(itemCount);
                        inventory.put(slot, stack);

                        itemData.counter = itemData.counter + itemCount;
                    }
                }
                else {
                    if (getRandom(0f, 100f) < itemData.chance) {
                        int itemCount = getRandom(1, itemData.maxCount - itemData.counter);

                        ItemStack stack = itemData.item.clone();
                        stack.setAmount(itemCount);
                        inventory.put(slot, stack);
                        itemData.counter = itemData.counter + itemCount;
                    }
                }
            }

            slotList.remove(index);
        }

        return inventory;
    }

    public boolean testBarrelCatch() {
        if (!isEnable) {
            return false;
        }

        return getRandom(0, 100) <= catchChance;
    }

    private int getRandom(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    private float getRandom(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    @AllArgsConstructor
    @Getter
    private static class ItemSettings {
        private ItemStack item;
        private float chance;
        private int minCount;
        private int maxCount;
        @Setter
        private int counter;
    }
}
