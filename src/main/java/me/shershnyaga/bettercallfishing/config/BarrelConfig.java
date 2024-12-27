package me.shershnyaga.bettercallfishing.config;

import lombok.Getter;
import lombok.SneakyThrows;
import me.shershnyaga.bettercallfishing.config.parser.ItemStackParser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class BarrelConfig {

    private final Random random = new Random();

    @Getter
    private boolean isEnable;
    private float catchChance;

    @Getter
    private List<ItemStackParser.ParsedItem> parsedItems = new ArrayList<>();

    private final ItemStackParser itemStackParser = new ItemStackParser(true,
            true, true, true);

    private File configFile;
    private FileConfiguration config;

    public BarrelConfig(File configFile) {

        this.configFile = configFile;
        config = YamlConfiguration.loadConfiguration(configFile);

        if (isNeedToUpdate()) {
            updateOldConfig();
        } else {
            loadItems();
        }

        parsedItems.clear();
        isEnable = config.getBoolean("enable-barrel-catch");
        catchChance = config.getInt("barrel-catch-chance");
    }

    public HashMap<Integer, ItemStack> generateBarrelInventoryMap() {
        HashMap<Integer, ItemStack> inventory = new HashMap<>();

        List<Integer> slotList = new ArrayList<>();

        for (int i=0; i<27; i++) {
            slotList.add(i);
        }

        Collections.shuffle(slotList);
        Queue<Integer> slotQueue = new LinkedList<>(slotList);

        List<ItemStackParser.ParsedItem> shuffledItems = new ArrayList<>(parsedItems);
        Collections.shuffle(shuffledItems);

        HashMap<ItemStackParser.ParsedItem, Integer> items = new HashMap<>();

        for (ItemStackParser.ParsedItem parsedItem : shuffledItems) {

            if (parsedItem.tryToGet()) {
                Optional<ItemStack> item = parsedItem.toItemStack();

                if (item.isPresent()) {
                    items.put(parsedItem, parsedItem.getRandomCountOrDefault());
                }
            }
        }

        while (!slotQueue.isEmpty()) {
            int slot = slotQueue.remove();

            for (ItemStackParser.ParsedItem parsedItem : items.keySet()) {
                int count = items.get(parsedItem);

                if (count <= 0) {
                    continue;
                }

                int rand = getRandom(1, count);
                items.put(parsedItem, count - rand);

                inventory.put(slot, parsedItem.toItemStack(rand).get());
            }
        }

        return inventory;
    }

    public boolean testBarrelCatch() {
        if (!isEnable) {
            return false;
        }

        return getRandom(0, 100) <= catchChance;
    }

    private void loadItems() {
        parsedItems = itemStackParser.parseItems((List<Map<String, Object>>) config.get("items"));
    }

    private boolean isNeedToUpdate() {
        return config.contains("barrel-items");
    }

    @SneakyThrows
    private void updateOldConfig() {
        BarrelConfigOld configOld = new BarrelConfigOld();

        List<BarrelConfigOld.ItemSettings> itemSettings = configOld.parseOldConfig(config);

        for (BarrelConfigOld.ItemSettings itemSetting : itemSettings) {
            ItemStackParser.ParsedItem.ParsedItemBuilder builder = ItemStackParser.ParsedItem.builder();

            builder.material(itemSetting.getId().toLowerCase());
            builder.chance(itemSetting.getChance());
            builder.minCount(itemSetting.getMinCount());
            builder.maxCount(itemSetting.getMaxCount());

            parsedItems.add(builder.build());
        }

        config.set("barrel-items", null);

        config.set("items", itemStackParser.dump(parsedItems));

        config.save(configFile);
    }

    private int getRandom(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    private float getRandom(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

}
