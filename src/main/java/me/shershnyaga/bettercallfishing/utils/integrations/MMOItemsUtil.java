package me.shershnyaga.bettercallfishing.utils.integrations;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class MMOItemsUtil {
    public static boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("MMOItems") != null;
    }

    public Optional<ItemStack> getItem(String type, String id) {

        if (!isEnabled()) {
            return Optional.empty();
        }

        MMOItem mmoitem = MMOItems.plugin.getMMOItem(Type.get(type), id);

        if (mmoitem != null) {
            return Optional.ofNullable(mmoitem.newBuilder().build());
        } else {
            return Optional.empty();
        }
    }
}
