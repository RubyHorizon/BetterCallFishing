package me.shershnyaga.bettercallfishing.config.parser.items;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.shershnyaga.bettercallfishing.utils.MessageUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemStackParser {

    private static final Random RANDOM = new Random();
    private static final PluginsItemsParser PLUGINS_ITEMS_PARSER = new PluginsItemsParser();

    private static final String MATERIAL_SECTION = "material";
    private static final String DISPLAY_NAME_SECTION = "name";
    private static final String COUNT_SECTION = "count";
    private static final String LORE_SECTION = "lore";
    private static final String CMD_SECTION = "cmd";
    private static final String ENCHANTMENTS_SECTION = "enchantments";
    private static final String POTION_DATA_SECTION = "potion-data";
    private static final String CHANCE_SECTION = "chance";

    private boolean enableChanceParse;
    private boolean enableCountRangeParse;

    private EnchantmentParser enchantmentParser;
    private PotionDataParser potionDataParser;

    public ItemStackParser(boolean enableChanceParse, boolean enableCountRangeParse,
                            boolean enableEnchantmentsRangeParse, boolean enableEnchantmentsChanceParse,
                           boolean enablePotionLevelRangeParse, boolean enablePotionChanceParse,
                           boolean enablePotionDurationSizeParse
                           ) {
        this.enableChanceParse = enableChanceParse;
        this.enableCountRangeParse = enableCountRangeParse;

        enchantmentParser = new EnchantmentParser(enableEnchantmentsRangeParse, enableEnchantmentsChanceParse);
        potionDataParser = new PotionDataParser(enablePotionLevelRangeParse, enablePotionChanceParse,
                enablePotionDurationSizeParse);
    }

    public List<ParsedItem> parseItems(List<Map<String, Object>> info) {
        List<ParsedItem> items = new ArrayList<>();

        for (Map<String, Object> item : info) {
            ParsedItem parsedItem = parse(item);

            if (parsedItem != null) {
                items.add(parsedItem);
            }
        }

        return items;
    }

    public ParsedItem parse(Map<String, Object> info) {
        ParsedItem.ParsedItemBuilder parsedItem = ParsedItem.builder();

        String material = String.valueOf(info.get(MATERIAL_SECTION));

        parsedItem.material(material);

        if (info.containsKey(DISPLAY_NAME_SECTION)) {
            String displayName = String.valueOf(info.get(DISPLAY_NAME_SECTION));
            parsedItem.displayName(displayName);
        }

        if (info.containsKey(LORE_SECTION)) {
            List<String> lore = (List<String>) info.get(LORE_SECTION);
            parsedItem.lore(lore);
        }

        if (info.containsKey(CMD_SECTION)) {
            parsedItem.cmd((int) info.get(CMD_SECTION));
        }

        if (info.containsKey(ENCHANTMENTS_SECTION)) {
            List<Map<String, Object>> enchants = (List<Map<String, Object>>) info.get(ENCHANTMENTS_SECTION);

            parsedItem.enchantments(enchants.stream().map(enchantmentParser::parse).toList());
        }

        /*
        if (info.containsKey(POTION_DATA_SECTION)) {
            List<Map<String, Object>> potionsData = (List<Map<String, Object>>) info.get(POTION_DATA_SECTION);

            parsedItem.parsedPotionData(potionsData.stream().map(potionDataParser::parse).toList());
        }
         */

        if (info.containsKey(COUNT_SECTION)) {
            String count = String.valueOf(info.get(COUNT_SECTION));

            if (enableCountRangeParse && count.contains("-")) {
                String[] parts = count.split("-");
                parsedItem.minCount(Integer.parseInt(parts[0]));
                parsedItem.maxCount(Integer.parseInt(parts[1]));
            } else {
                parsedItem.minCount(Integer.parseInt(count));
                parsedItem.maxCount(Integer.parseInt(count));
            }

        } else {
            parsedItem.minCount(1);
            parsedItem.maxCount(1);
        }

        if (info.containsKey(CHANCE_SECTION) && enableChanceParse) {
            String chance = String.valueOf(info.get(CHANCE_SECTION));
            parsedItem.chance(Float.parseFloat(chance));
        } else {
            parsedItem.chance(100f);
        }

        return parsedItem.build();
    }

    public Map<String, Object> dump(ParsedItem parsedItem) {
        Map<String, Object> dump = new HashMap<>();

        dump.put(MATERIAL_SECTION, parsedItem.material);

        if (parsedItem.displayName != null) {
            dump.put(DISPLAY_NAME_SECTION, parsedItem.displayName);
        }

        if (parsedItem.lore != null && !parsedItem.lore.isEmpty()) {
            dump.put(LORE_SECTION, parsedItem.lore);
        }

        if (parsedItem.cmd != null) {
            dump.put(CMD_SECTION, parsedItem.cmd);
        }

        if (parsedItem.chance != 100f) {
            dump.put(CHANCE_SECTION, parsedItem.chance);
        }

        if (parsedItem.enchantments != null && !parsedItem.enchantments.isEmpty()) {
            List<Map<String, Object>> enchants = new ArrayList<>();

            for (EnchantmentParser.ParsedEnchantment enchantment : parsedItem.enchantments) {
                enchants.add(enchantmentParser.dump(enchantment));
            }

            dump.put(ENCHANTMENTS_SECTION, enchants);
        }

        if (enableCountRangeParse && parsedItem.minCount != parsedItem.maxCount) {
            dump.put(COUNT_SECTION, parsedItem.minCount + "-" + parsedItem.maxCount);
        } else {
            dump.put(COUNT_SECTION, parsedItem.minCount);
        }

        return dump;
    }

    public List<Map<String, Object>> dump(List<ParsedItem> parsedItems) {
        List<Map<String, Object>> dump = new ArrayList<>();

        for (ParsedItem parsedItem : parsedItems) {
            dump.add(dump(parsedItem));
        }

        return dump;
    }

    @lombok.Builder(access = AccessLevel.PUBLIC)
    public static class ParsedItem {

        @Getter
        private String material;

        private String displayName;
        private List<String> lore;

        @Getter
        private Integer cmd;

        @Getter
        private List<EnchantmentParser.ParsedEnchantment> enchantments;

        @Getter
        private int minCount;

        @Getter
        private int maxCount;

        @Getter
        private float chance;

        @Getter
        private List<PotionDataParser.ParsedPotionData> parsedPotionData;

        public Optional<ItemStack> toItemStack() {
            return toItemStack(1);
        }

        public Optional<ItemStack> toItemStack(int count) {
            Optional<ItemStack> itemStack = PLUGINS_ITEMS_PARSER.parseFromString(material);

            if (itemStack.isEmpty()) {
                return Optional.empty();
            }
            ItemStack item = itemStack.get();

            item.setAmount(count);

            ItemMeta meta = item.getItemMeta();
            if (displayName != null) {
                meta.setDisplayName(getDisplayName());
            }

            if (lore != null) {
                meta.setLore(getLore());
            }

            if (cmd != null) {
                meta.setCustomModelData(cmd);
            }

            item.setItemMeta(meta);
            if (enchantments != null) {
                enchantments.forEach(enchantment -> enchantment.tryToAddEnchant(item));
            }

            if (parsedPotionData != null) {
                parsedPotionData.forEach(data -> data.tryToAddEffect(item));
            }

            return Optional.of(item);
        }

        public boolean tryToGet() {
            return getRandom(0f, 100f) <= chance || chance == 100f;
        }

        public int getRandomCountOrDefault() {
            int count;
            if (minCount == maxCount) {
                count = minCount;
            } else {
                count = getRandom(minCount, maxCount);
            }

            return count;
        }

        public List<String> getLore() {

            if (lore == null) {
                return null;
            }

            return lore.stream().map(MessageUtils::parseChatColors).toList();
        }

        public String getDisplayName() {

            if (displayName == null) {
                return null;
            }

            return MessageUtils.parseChatColors(displayName);
        }

        public Optional<ItemStack> toItemStackWithRandomCount() {
            return toItemStack(getRandomCountOrDefault());
        }

        private float getRandom(float min, float max) {
            return min + RANDOM.nextFloat() * (max - min);
        }

        private int getRandom(int min, int max) {
            return RANDOM.nextInt((max - min) + 1) + min;
        }
    }

    @Setter
    public static class Builder {
        private boolean enableChanceParse = false;
        private boolean enableCountRangeParse = false;
        private boolean enableEnchantmentsRangeParse;
        private boolean enableEnchantmentsChanceParse;
        private boolean enablePotionLevelRangeParse;
        private boolean enablePotionChanceParse;
        private boolean enablePotionDurationSizeParse;

        public Builder() {

        }

        public static Builder builder() {
            return new Builder();
        }

        public ItemStackParser build() {
            return new ItemStackParser(enableChanceParse,
                    enableCountRangeParse,
                    enableEnchantmentsRangeParse,
                    enableEnchantmentsChanceParse,
                    enablePotionLevelRangeParse,
                    enablePotionChanceParse,
                    enablePotionDurationSizeParse);
        }
    }

}
