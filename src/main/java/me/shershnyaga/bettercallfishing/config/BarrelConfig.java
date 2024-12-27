package me.shershnyaga.bettercallfishing.config;

import lombok.Getter;
import lombok.SneakyThrows;
import me.shershnyaga.bettercallfishing.config.parser.ItemStackParser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BarrelConfig {

    @Getter
    private boolean isEnable;
    private float catchChance;

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
}
