package me.shershnyaga.bettercallfishing.config;

import lombok.Getter;
import me.shershnyaga.bettercallfishing.config.parser.items.ItemStackParser;
import me.shershnyaga.bettercallfishing.utils.chances.ChanceUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class CaughtItemsConfig {

    private static final ChanceUtils<ItemStackParser.ParsedItem> CHANCE_UTILS = new ChanceUtils<>(100);

    private List<ItemStackParser.ParsedItem> parsedItems;

    @Getter
    private boolean disableDefaultItemsCatch;

    private final ItemStackParser itemStackParser = new ItemStackParser(true,
            true, true, true,
            false, false, false);

    public CaughtItemsConfig(File configFile) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        disableDefaultItemsCatch = config.getBoolean("disable-default-catch-items");

        loadItems(config);
    }

    private void loadItems(FileConfiguration config) {
        parsedItems = itemStackParser.parseItems((List<Map<String, Object>>) config.get("items"));
        Collections.shuffle(parsedItems);
    }

    public Optional<ItemStack> tryToGetRandomItem() {
        Optional<ItemStackParser.ParsedItem> item =
                CHANCE_UTILS.tryToGetRandomItem(parsedItems.toArray(new ItemStackParser.ParsedItem[0]));

        if (item.isEmpty()) {
            return Optional.empty();
        }

        return item.get().toItemStackWithRandomCount();
    }

    public Optional<ItemStack> getRandomItem() {
        Optional<ItemStackParser.ParsedItem> item =
                CHANCE_UTILS.getRandomItem(parsedItems.toArray(new ItemStackParser.ParsedItem[0]));

        if (item.isEmpty()) {
            return Optional.empty();
        }

        return item.get().toItemStackWithRandomCount();
    }
}
