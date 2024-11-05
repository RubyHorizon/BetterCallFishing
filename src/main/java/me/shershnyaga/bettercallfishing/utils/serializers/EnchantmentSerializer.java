package me.shershnyaga.bettercallfishing.utils.serializers;

import lombok.Getter;
import net.elytrium.serializer.language.object.YamlSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public class EnchantmentSerializer extends YamlSerializable {
    private String enchantment;
    private int lvl;
    private boolean hide;

    public void append(ItemMeta meta) {
        Enchantment enchantment = Enchantment.getByName(this.enchantment);
        meta.addEnchant(enchantment, lvl, hide);
    }
}
