package me.shershnyaga.bettercallfishing;

import me.shershnyaga.bettercallfishing.config.parser.EnchantmentParser;
import me.shershnyaga.bettercallfishing.config.parser.ItemStackParser;
import me.shershnyaga.bettercallfishing.utils.MiniMessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@DisplayName("Item Parser Test")
public class ItemParserConfigTest {

    @Test
    void testItemParserConfig() {
        ItemStackParser.Builder builder = ItemStackParser.Builder.builder();

        builder.setEnableChanceParse(true);
        builder.setEnableCountRangeParse(true);
        builder.setEnableEnchantmentsChanceParse(true);
        builder.setEnableEnchantmentsRangeParse(true);

        ItemStackParser itemParser = builder.build();

        File file = new File("src/test/resources/test_item.yml");

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<ItemStackParser.ParsedItem> items = itemParser.parseItems((List<Map<String, Object>>) config.get("test-items"));

        System.out.println(items.size());

        items.forEach(item -> {
            System.out.println("--------------------");
            System.out.println("Material: " + item.getMaterial());

            if (item.getDisplayName() != null) {
                System.out.println("DisplayName: " + item.getDisplayName());
            }

            if (item.getLore() != null && !item.getLore().isEmpty()) {
                System.out.println("Lore: ");
                item.getLore().forEach(System.out::println);
            }
            System.out.println("Cmd: " + item.getCmd());
            System.out.println("minCount: " + item.getMinCount());
            System.out.println("maxCount: " + item.getMaxCount());
            System.out.println("Chance: " + item.getChance());

            if (item.getEnchantments() != null && !item.getEnchantments().isEmpty()) {
                System.out.println("Enchantments: ");
                List<EnchantmentParser.ParsedEnchantment> enchantments = item.getEnchantments();

                enchantments.forEach(enchantment -> {
                    System.out.println("id: " + enchantment.getEnchantment());
                    System.out.println("chance: " + enchantment.getChance());
                    System.out.println("minLvl: " + enchantment.getMinLvl());
                    System.out.println("maxLvl: " + enchantment.getMaxLvl());
                    System.out.println("---");
                });
            }

        });
    }

    @Test
    void testDump() {
        ItemStackParser.Builder builder = ItemStackParser.Builder.builder();

        builder.setEnableChanceParse(true);
        builder.setEnableCountRangeParse(true);
        builder.setEnableEnchantmentsChanceParse(true);
        builder.setEnableEnchantmentsRangeParse(true);

        ItemStackParser itemParser = builder.build();

        File file = new File("src/test/resources/test_item.yml");

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<ItemStackParser.ParsedItem> items = itemParser.parseItems((List<Map<String, Object>>) config.get("test-items"));

        File output = new File("src/test/resources/test.yml");
        YamlConfiguration outputConfig = YamlConfiguration.loadConfiguration(output);

        outputConfig.set("test", items.stream().map(itemParser::dump).toList());
        try {
            outputConfig.save(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
