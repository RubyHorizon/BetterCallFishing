package me.shershnyaga.bettercallfishing.config.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class EnchantmentParser {

    public static ParsedEnchantment parse(ConfigurationSection section) {
        Enchantment enchantment = Enchantment.getByName("enchantment");
        int level = section.getInt("level");

        return new ParsedEnchantment(enchantment, level);
    }

    @Getter
    public record ParsedEnchantment(Enchantment enchantment, int level) {
        public ItemStack addIntoItem(ItemStack item) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.addEnchant(enchantment, level, true);
                item.setItemMeta(meta);
            }

            return item;
        }
    }

}
