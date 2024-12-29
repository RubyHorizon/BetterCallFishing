package me.shershnyaga.bettercallfishing.config.parser.items;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import me.shershnyaga.bettercallfishing.BetterCallFishing;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

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

        if (enchantmentInfo == null) {
            builder.minLvl(1);
            builder.maxLvl(1);
            builder.chance(100);
            builder.enchantment(enchant);
            return builder.build();
        }

        if (enchantmentInfo.containsKey(LEVEL_SECTION)) {
            String level = String.valueOf(enchantmentInfo.get(LEVEL_SECTION));
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

            String chance = String.valueOf(enchantmentInfo.get(CHANCE_SECTION));

            builder.chance(Float.parseFloat(chance));

        } else {
            builder.chance(100);
        }

        builder.enchantment(enchant);

        return builder.build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public static class ParsedEnchantment {

        private static final Random random = new Random();

        private String enchantment;

        @Getter
        private int minLvl;

        @Getter
        private int maxLvl;

        @Getter
        private float chance;

        public ItemStack addEnchantment(ItemStack item) {
            int level;
            if (minLvl == maxLvl) {
                level = minLvl;
            } else {
                level = getRandom(minLvl, maxLvl);
            }
            ItemMeta meta = item.getItemMeta();

            Enchantment enchant = getEnchantment();

            if (enchant == null) {
                return item;
            }

            if (item.getType() == Material.ENCHANTED_BOOK) {
                if (meta instanceof EnchantmentStorageMeta bookMeta) {
                    bookMeta.addStoredEnchant(enchant, level, true);
                    item.setItemMeta(bookMeta);
                }

            } else {
                Objects.requireNonNull(meta).addEnchant(enchant, level, true);
                item.setItemMeta(meta);
            }

            return item;
        }

        public Enchantment getEnchantment() {
            Enchantment e = Enchantment.getByName(enchantment);

            if (e != null) {
                return e;
            } else {
                BetterCallFishing.log(ChatColor.RED + "Cannot find enchantment \"" + enchantment + "\"");
                return null;
            }
        }

        public String getEnchantmentName() {
            return enchantment;
        }

        public ItemStack tryToAddEnchant(ItemStack item) {
            if (chance == 100f) {
                return addEnchantment(item);
            }

            if (chance >= getRandom(0f, 100f)) {
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

    public Map<String, Object> dump(ParsedEnchantment enchantment) {
        Map<String, Object> dump = new HashMap<>();

        Map<String, Object> info = new HashMap<>();

        if (enableChanceParse && enchantment.chance != 100f) {
            info.put(CHANCE_SECTION, enchantment.chance);
        }

        if (enableLevelRangeParse && enchantment.minLvl != enchantment.maxLvl) {
            info.put(LEVEL_SECTION, enchantment.minLvl + "-" + enchantment.maxLvl);
        } else {
            info.put(LEVEL_SECTION, enchantment.minLvl);
        }

        dump.put(enchantment.getEnchantmentName().toLowerCase(), info);

        return dump;
    }

}
