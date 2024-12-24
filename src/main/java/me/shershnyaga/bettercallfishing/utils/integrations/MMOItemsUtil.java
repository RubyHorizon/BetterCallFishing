package me.shershnyaga.bettercallfishing.utils.integrations;

import me.shershnyaga.bettercallfishing.BetterCallFishing;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class MMOItemsUtil {
    public static boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("MMOItems") != null;
    }

    public Optional<ItemStack> getItem(String type, String id) {

        if (!isEnabled()) {
            BetterCallFishing.log(ChatColor.RED + "\""
                    + id + "\" this is an MMOItems item, but the MMOItems plugin " +
                    "is not loaded!");
            return Optional.empty();
        }

        MMOItem mmoitem = MMOItems.plugin.getMMOItem(Type.get(type), id);

        if (mmoitem != null) {

            ItemStack item = mmoitem.newBuilder().build();

            if (item != null) {
                return Optional.of(item);
            } else {
                BetterCallFishing.log(ChatColor.RED + "\""
                        + id + "\" is not registered in MMOItems!");
            }

        } else {
            BetterCallFishing.log(ChatColor.RED + "\""
                    + id + "\" is not registered in MMOItems!");
        }

        return Optional.empty();
    }
}
