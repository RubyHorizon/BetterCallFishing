package me.shershnyaga.bettercallfishing.config.parser;

import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class ItemStackParser {

    private static final String MATERIAL_SECTION = "material";
    private static final String LORE_SECTION = "lore";
    private static final String CMD_SECTION = "cmd";

    public static ParsedItem parse(ConfigurationSection section) {

        String materialName = section.getString(MATERIAL_SECTION);
        if (materialName.contains("IA:")) {

        }

        ItemStack parsedItem = new ItemStack(Material.AIR);

        String materialName = section.getString("material");
        List<String> lore = section.getStringList("lore");
        int cmd = section.getInt("custom-model-data");
    }


    @AllArgsConstructor
    public static class ParsedItem {

        private final ItemStack item;

        public ItemStack toItemStack() {
            return toItemStack(1);
        }

        public ItemStack toItemStack(int count) {
            ItemStack i = item.clone();
            i.setAmount(count);
            return i;
        }

        public ConfigurationSection dump() {
            return null;
        }
    }

}
