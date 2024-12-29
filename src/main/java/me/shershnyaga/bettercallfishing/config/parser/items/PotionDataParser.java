package me.shershnyaga.bettercallfishing.config.parser.items;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import me.shershnyaga.bettercallfishing.BetterCallFishing;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Random;

public class PotionDataParser {

    private static final String CHANCE_SECTION = "chance";
    private static final String LEVEL_SECTION = "level";
    private static final String DURATION_SECTION = "duration";

    private boolean enableLevelRangeParse;
    private boolean enableChanceParse;
    private boolean enableDurationSizeParse;

    public PotionDataParser(boolean enableLevelRangeParse, boolean enableChanceParse, boolean enableDurationSizeParse) {
        this.enableLevelRangeParse = enableLevelRangeParse;
        this.enableChanceParse = enableChanceParse;
        this.enableDurationSizeParse = enableDurationSizeParse;
    }

    public ParsedPotionData parse(Map<String, Object> potionData) {
        ParsedPotionData.ParsedPotionDataBuilder builder = ParsedPotionData.builder();

        String effect = potionData.keySet().iterator().next();

        Map<String, Object> potionInfo = (Map<String, Object>) potionData.get(effect);

        builder.effect(effect);

        if (potionInfo == null) {
            builder.minLvl(1);
            builder.maxLvl(1);
            builder.minDuration(20);
            builder.maxDuration(20);
            builder.chance(100);
            return builder.build();
        }

        if (potionInfo.containsKey(LEVEL_SECTION)) {
            String level = String.valueOf(potionInfo.get(LEVEL_SECTION));
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

        if (potionInfo.containsKey(CHANCE_SECTION) && enableChanceParse) {

            String chance = String.valueOf(potionInfo.get(CHANCE_SECTION));

            builder.chance(Float.parseFloat(chance));

        } else {
            builder.chance(100);
        }

        if (potionInfo.containsKey(DURATION_SECTION)) {
            String dur = String.valueOf(potionInfo.get(DURATION_SECTION));
            String[] durSplit = dur.split("-");

            if (enableDurationSizeParse && durSplit.length == 2) {
                builder.minLvl(Integer.parseInt(durSplit[0]));
                builder.maxLvl(Integer.parseInt(durSplit[1]));
            } else {
                builder.minLvl(Integer.parseInt(dur));
                builder.maxLvl(Integer.parseInt(dur));
            }
        } else {
            builder.minDuration(1);
            builder.maxDuration(1);
        }

        return builder.build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public static class ParsedPotionData {
        private static final Random random = new Random();

        private String effect;

        @Getter
        private int minLvl;

        @Getter
        private int maxLvl;

        @Getter
        private int minDuration;

        @Getter
        private int maxDuration;

        @Getter
        private float chance;

        public ItemStack addEffect(ItemStack itemStack) {
            if (itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
                int level;
                if (minLvl == maxLvl) {
                    level = minLvl;
                } else {
                    level = getRandom(minLvl, maxLvl);
                }

                int duration;
                if (minDuration == maxDuration) {
                    duration = minDuration;
                } else {
                    duration = getRandom(minDuration, maxDuration) * 20;
                }


                PotionEffectType effectType = PotionEffectType.getByName(effect);

                if (effectType != null) {
                    potionMeta.addCustomEffect(new PotionEffect(getEffect(), duration, level), true);
                }

                itemStack.setItemMeta(potionMeta);
            }

            return itemStack;
        }

        public ItemStack tryToAddEffect(ItemStack item) {
            if (chance == 100f) {
                return addEffect(item);
            }

            if (chance >= getRandom(0f, 100f)) {
                addEffect(item);
            }

            return item;
        }

        public PotionEffectType getEffect() {
            PotionEffectType e = PotionEffectType.getByName(effect);

            if (e != null) {
                return e;
            } else {
                BetterCallFishing.log(ChatColor.RED + "Cannot find Potion effect \"" + e.getName() + "\"");
                return null;
            }
        }

        private float getRandom(float min, float max) {
            return min + random.nextFloat() * (max - min);
        }

        private int getRandom(int min, int max) {
            return random.nextInt((max - min) + 1) + min;
        }
    }
}
