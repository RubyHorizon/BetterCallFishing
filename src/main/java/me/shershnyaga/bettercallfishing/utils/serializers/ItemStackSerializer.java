package me.shershnyaga.bettercallfishing.utils.serializers;

import lombok.Getter;
import me.shershnyaga.bettercallfishing.utils.KyoriUtils;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.language.object.YamlSerializable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ItemStackSerializer extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder().build();

    private String material;
    private int count;
    private String displayName;
    private List<String> lore;
    private List<EnchantmentSerializer> enchantments = new ArrayList<>();
    private Integer cmd;

    public ItemStackSerializer() {
        super(CONFIG);
    }

    private void addEnchantments(EnchantmentSerializer... enchantments) {
        for (EnchantmentSerializer enchantment: enchantments) {

            boolean set = true;
            for (int i=0; i<this.enchantments.size(); i++) {
                if (enchantment.getEnchantment().equals(this.enchantments.get(i).getEnchantment())) {
                    this.enchantments.set(i, enchantment);
                    set = false;
                }
            }

            if (set) {
                this.enchantments.add(enchantment);
            }

        }
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(Material.valueOf(material), count);

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        if (displayName != null) {
            meta.setDisplayName(KyoriUtils.translateMiniMessage(displayName));
        }

        if (!lore.isEmpty()) {
            meta.setLore(lore.stream().map(KyoriUtils::translateMiniMessage).toList());
        }

        if (cmd != null) {
            meta.setCustomModelData(cmd);
        }

        if (!enchantments.isEmpty()) {
            enchantments.forEach(e -> e.append(meta));
        }

        item.setItemMeta(meta);

        return item;
    }

}
