package me.shershnyaga.bettercallfishing.config;

import lombok.Getter;
import me.shershnyaga.bettercallfishing.config.parser.items.ItemStackParser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class CaughtItemsConfig {

    private List<ItemStackParser.ParsedItem> parsedItems;

    @Getter
    private boolean disableDefaultItemsCatch;

    private final ItemStackParser itemStackParser = new ItemStackParser(true,
            true, true, true,
            true, true, true);

    public CaughtItemsConfig(File configFile) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        disableDefaultItemsCatch = config.getBoolean("disable-default-catch-items");

        loadItems(config);
    }

    private void loadItems(FileConfiguration config) {
        parsedItems = itemStackParser.parseItems((List<Map<String, Object>>) config.get("items"));
        parsedItems.sort(Comparator.comparing(ItemStackParser.ParsedItem::getChance));
    }

    public Optional<ItemStack> tryToGetRandomItem() {

        List<ItemStackParser.ParsedItem> items = new ArrayList<>(parsedItems);

        for (ItemStackParser.ParsedItem item: items) {
            if (item.tryToGet()) {
                return item.toItemStackWithRandomCount();
            }
        }

        return Optional.empty();
    }

    public ItemStack getRandomItem() {
        if ()
    }
}
