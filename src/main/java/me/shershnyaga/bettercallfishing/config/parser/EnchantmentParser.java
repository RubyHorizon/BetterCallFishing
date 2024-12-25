package me.shershnyaga.bettercallfishing.config.parser;

import lombok.AccessLevel;
import lombok.Builder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class EnchantmentParser {

    private static final String CHANCE_SECTION = "chance";
    private static final String LEVEL_SECTION = "level";

    private boolean enableLevelRangeParse;
    private boolean enableChanceParse;

    public EnchantmentParser(boolean enableLevelRangeParse, boolean enableChanceParse) {
        this.enableLevelRangeParse = enableLevelRangeParse;
        this.enableChanceParse = enableChanceParse;
    }

    public ParsedEnchantment parse(Map<String, Object> enchantment) {

        ParsedEnchantment.ParsedEnchantmentBuilder builder = ParsedEnchantment.builder();

        String enchant = enchantment.keySet().iterator().next();

        Map<String, Object> enchantmentInfo = (Map<String, Object>) enchantment.get(enchant);

        if (enchantmentInfo.containsKey(LEVEL_SECTION)) {
            String level = (String) enchantmentInfo.get(LEVEL_SECTION);
            String[] levelSplit = level.split("-");

            if (enableLevelRangeParse && levelSplit.length == 2) {
                builder.minLvl(Integer.parseInt(levelSplit[0]));
                builder.maxLvl(Integer.parseInt(levelSplit[1]));
            } else {
                builder.minLvl(Integer.parseInt(level));
                builder.maxLvl(Integer.parseInt(level));
            }
        } else {
            builder.minLvl(1);
            builder.maxLvl(1);
        }

        if (enchantmentInfo.containsKey(CHANCE_SECTION) && enableChanceParse) {

            builder.chance((float) enchantmentInfo.get(CHANCE_SECTION));

        } else {
            builder.chance(100);
        }

        return builder.build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public static class ParsedEnchantment {

        private Random random = new Random();

        Enchantment enchantment;

        private int minLvl;
        private int maxLvl;

        private float chance;

        private ParsedEnchantment() {

        }

        public ItemStack addEnchantment(ItemStack item) {
            int level;
            if (minLvl == maxLvl) {
                level = minLvl;
            } else {
                level = getRandom(minLvl, maxLvl);
            }

            ItemMeta meta = item.getItemMeta();

            Objects.requireNonNull(meta).addEnchant(enchantment, level, true);
            item.setItemMeta(meta);

            return item;
        }

        public ItemStack tryToAddEnchant(ItemStack item) {
            if (chance == 100f) {
                return addEnchantment(item);
            }

            if (chance >= getRandom(0, 100)) {
                addEnchantment(item);
            }

            return item;
        }

        private float getRandom(float min, float max) {
            return min + random.nextFloat() * (max - min);
        }

        private int getRandom(int min, int max) {
            return random.nextInt((max - min) + 1) + min;
        }
    }

}
