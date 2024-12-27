package me.shershnyaga.bettercallfishing.config.parser;

import lombok.NoArgsConstructor;
import me.shershnyaga.bettercallfishing.hooks.ItemsAdderHook;
import me.shershnyaga.bettercallfishing.hooks.MMOItemsHook;
import me.shershnyaga.bettercallfishing.hooks.PluginHooks;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@NoArgsConstructor
public class PluginsItemsParser {

    public Optional<ItemStack> parseFromString(String string) {
        if (!string.contains(":")) {
            Material material = Material.matchMaterial(string.toUpperCase());

            if (material == null) {
                return Optional.empty();
            } else {
                return Optional.of(new ItemStack(material));
            }
        }

        String[] split = string.split(":");

        if (split[0].equals("IA")) {
            return getIAItem(split[1]);
        } else if (split[0].equals("MMO")) {
            return getMMOItem(split[1], split[2]);
        }

        return Optional.empty();
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
