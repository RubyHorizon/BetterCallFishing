package me.shershnyaga.bettercallfishing.config;

import dev.lone.itemsadder.api.CustomStack;
import lombok.*;
import me.shershnyaga.bettercallfishing.utils.integrations.ItemsAdderUtil;
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

    private void setConfiguration(FileConfiguration config) {
        itemSettingsList.clear();
        isEnable = config.getBoolean("enable-barrel-catch");
        catchChance = config.getInt("barrel-catch-chance");

        Set<String> keys = config.getConfigurationSection("barrel-items").getKeys(false);

        boolean isIaEnabled = ItemsAdderUtil.isEnabled();

        for (String key: keys) {
            int chance = config.getInt("barrel-items." + key + ".chance");
            int minCount = config.getInt("barrel-items." + key + ".min-count");
            int maxCount = config.getInt("barrel-items." + key + ".max-count");

            if (key.startsWith("IA:")) {
                if (!isIaEnabled) {
                    Bukkit.getLogger().info(ChatColor.RED + "[BetterCallFishing] \""
                            + key + "\" this is an ItemsAdder item, but the ItemsAdder plugin " +
                            "is not loaded!!");
                    continue;
                }

            }

            ItemSettings settings = new ItemSettings(key, chance, minCount, maxCount, 0);
            itemSettingsList.add(settings);
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

            if (i.getItem().isPresent()) {
                itemSettings.add(new ItemSettings(i.id, i.chance, i.minCount, i.maxCount, 0));
            }
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

                        if (itemData.getItem().isPresent()) {
                            ItemStack stack = itemData.getItem().get();
                            stack.setAmount(itemCount);
                            inventory.put(slot, stack);

                            itemData.counter = itemData.counter + itemCount;
                        }
                    }
                }
                else {
                    if (getRandom(0f, 100f) < itemData.chance) {
                        int itemCount = getRandom(1, itemData.maxCount - itemData.counter);

                        if (itemData.getItem().isPresent()) {
                            ItemStack stack = itemData.getItem().get();
                            stack.setAmount(itemCount);
                            inventory.put(slot, stack);

                            itemData.counter = itemData.counter + itemCount;
                        }
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
    private static class ItemSettings {

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
                return getIAItem(id, amount);
            } else if (Material.matchMaterial(id) != null) {
                return Optional.of(new ItemStack(Objects.requireNonNull(Material.getMaterial(id)), amount));
            } else {

                return Optional.empty();
            }
        }

        private Optional<ItemStack> getIAItem(String id, int amount) {
            if (ItemsAdderUtil.isEnabled()) {
                String iaId = id.replace("IA:", "");
                if (CustomStack.isInRegistry(iaId)) {

                    ItemStack item = CustomStack.getInstance(iaId).getItemStack().clone();
                    item.setAmount(amount);

                    return Optional.of(item);
                }

                Bukkit.getLogger().info(ChatColor.RED + "[BetterCallFishing] \""
                        + id + "\" is not registered in ItemsAdder!");

            } else {
                Bukkit.getLogger().info(ChatColor.RED + "[BetterCallFishing] \""
                        + id + "\" this is an ItemsAdder item, but the ItemsAdder plugin " +
                        "is not loaded!!");
            }

            return Optional.empty();
        }
    }
}
