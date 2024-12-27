package me.shershnyaga.bettercallfishing.config.parser;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.shershnyaga.bettercallfishing.BetterCallFishing;
import me.shershnyaga.bettercallfishing.hooks.PluginHooks;
import me.shershnyaga.bettercallfishing.hooks.ItemsAdderHook;
import me.shershnyaga.bettercallfishing.hooks.MMOItemsHook;
import me.shershnyaga.bettercallfishing.utils.MiniMessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemStackParser {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final String MATERIAL_SECTION = "material";
    private static final String DISPLAY_NAME_SECTION = "name";
    private static final String COUNT_SECTION = "count";
    private static final String LORE_SECTION = "lore";
    private static final String CMD_SECTION = "cmd";
    private static final String ENCHANTMENTS_SECTION = "enchantments";
    private static final String CHANCE_SECTION = "chance";

    private boolean enableChanceParse;
    private boolean enableCountRangeParse;

    private EnchantmentParser enchantmentParser;

    private ItemStackParser(boolean enableChanceParse, boolean enableCountRangeParse,
                            boolean enableEnchantmentsRangeParse, boolean enableEnchantmentsChanceParse) {
        this.enableChanceParse = enableChanceParse;
        this.enableCountRangeParse = enableCountRangeParse;

        enchantmentParser = new EnchantmentParser(enableEnchantmentsRangeParse, enableEnchantmentsChanceParse);
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

        parsedItem.material((String) info.get(MATERIAL_SECTION));

        if (info.containsKey(DISPLAY_NAME_SECTION)) {
            parsedItem.displayName(MINI_MESSAGE.deserialize((String) info.get(DISPLAY_NAME_SECTION)));
        }

        if (info.containsKey(LORE_SECTION)) {
            List<String> lore = (List<String>) info.get(LORE_SECTION);
            parsedItem.lore(lore.stream().map(MINI_MESSAGE::deserialize).toList());
        }

        if (info.containsKey(CMD_SECTION)) {
            parsedItem.cmd((int) info.get(CMD_SECTION));
        }

        if (info.containsKey(ENCHANTMENTS_SECTION)) {
            List<Map<String, Object>> enchants = (List<Map<String, Object>>) info.get(ENCHANTMENTS_SECTION);

            parsedItem.enchantments(enchants.stream().map(enchantmentParser::parse).toList());
        }

        if (info.containsKey(COUNT_SECTION)) {
            String count = (String) info.get(COUNT_SECTION);

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
            double chance = (double) info.get(CHANCE_SECTION);
            parsedItem.chance((float) chance);
        } else {
            parsedItem.chance(100f);
        }

        return parsedItem.build();
    }

    @lombok.Builder(access = AccessLevel.PRIVATE)
    @Getter
    public static class ParsedItem {
        private static final Random random = new Random();

        private String material;

        private Component displayName;
        private List<Component> lore;
        private Integer cmd;
        private List<EnchantmentParser.ParsedEnchantment> enchantments;

        private int minCount;
        private int maxCount;

        private float chance;

        public Optional<ItemStack> toItemStack() {
            return toItemStack(1);
        }

        public Optional<ItemStack> toItemStack(int count) {
            ItemStack item;
            if (material.contains(":")) {
                String[] parts = material.split(":");

                if (parts[0].equals("IA")) {
                    Optional<ItemStack> ia = getIAItem(parts[1]);

                    if (ia.isPresent()) {
                        item = ia.get();
                    } else {
                        return Optional.empty();
                    }
                } else if (parts[0].equals("MMO")) {
                    Optional<ItemStack> mmo = getMMOItem(parts[1], parts[2]);

                    if (mmo.isPresent()) {
                        item = mmo.get();
                    } else {
                        return Optional.empty();
                    }
                } else {
                    BetterCallFishing.log(ChatColor.RED + "Unable to parse material: \"" + material + "\" because " + parts[0] +
                            " is not a valid extension!");
                    return Optional.empty();
                }
            } else {
                item = new ItemStack(Material.valueOf(material), count);
            }

            item.setAmount(count);

            ItemMeta meta = item.getItemMeta();
            if (displayName != null) {
                meta.setDisplayName(MiniMessageUtils.convertComponentToString(displayName));
            }

            if (lore != null) {
                meta.setLore(lore.stream().map(MiniMessageUtils::convertComponentToString).toList());
            }

            if (cmd != null) {
                meta.setCustomModelData(cmd);
            }

            enchantments.forEach(enchantment -> enchantment.tryToAddEnchant(item));

            return Optional.of(item);
        }

        public Optional<ItemStack> getWithChances() {
            if (getRandom(0f, 100f) > chance && chance != 100f) {
                return Optional.empty();
            }

            int count;
            if (minCount == maxCount) {
                count = minCount;
            } else {
                count = getRandom(minCount, maxCount);
            }

            return toItemStack(count);
        }

        private float getRandom(float min, float max) {
            return min + random.nextFloat() * (max - min);
        }

        private int getRandom(int min, int max) {
            return random.nextInt((max - min) + 1) + min;
        }

        private Optional<ItemStack> getIAItem(String id) {
            ItemsAdderHook itemsAdderHook = (ItemsAdderHook) PluginHooks.ITEMS_ADDER.getHook();
            return itemsAdderHook.getItem(id);
        }

        private Optional<ItemStack> getMMOItem(String type, String id) {
            MMOItemsHook mmoItemsHook = (MMOItemsHook) PluginHooks.MMO_ITEMS.getHook();
            return mmoItemsHook.getItem(type, id);
        }
    }

    @Setter
    public static class Builder {
        private boolean enableChanceParse = false;
        private boolean enableCountRangeParse = false;
        private boolean enableEnchantmentsRangeParse;
        private boolean enableEnchantmentsChanceParse;

        private Builder() {

        }

        public static Builder builder() {
            return new Builder();
        }

        public ItemStackParser build() {
            return new ItemStackParser(enableChanceParse,
                    enableCountRangeParse,
                    enableEnchantmentsRangeParse,
                    enableEnchantmentsChanceParse);
        }
    }

}
