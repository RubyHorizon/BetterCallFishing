package me.shershnyaga.bettercallfishing.config;

import lombok.Getter;
import me.shershnyaga.bettercallfishing.config.parser.items.ItemStackParser;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class CaughtItemsConfig {

    @Getter
    private List<ItemStackParser.ParsedItem> parsedItems;

    @Getter
    private boolean disableDefaultItemsCatch;

    public CaughtItemsConfig(FileConfiguration config) {



        parsedItems =
    }
}
