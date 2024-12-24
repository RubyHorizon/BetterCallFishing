package me.shershnyaga.bettercallfishing.config.parser;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ItemStackParser {

    public static ParsedItem parse(ConfigurationSection section) {
        String materialName = section.getString("material");
        List<String> lore = section.getStringList("lore");
        int cmd = section.getInt("custom-model-data");
    }


    public static class ParsedItem {
        public ItemStack toItemStack() {
            return toItemStack(1);
        }

        public ItemStack toItemStack(int count) {
            return null;
        }

        public ConfigurationSection dump() {
            return null;
        }
    }

}
