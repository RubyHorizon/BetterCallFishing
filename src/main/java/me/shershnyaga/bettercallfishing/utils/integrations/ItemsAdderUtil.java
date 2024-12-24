package me.shershnyaga.bettercallfishing.utils.integrations;

import dev.lone.itemsadder.api.CustomStack;
import me.shershnyaga.bettercallfishing.BetterCallFishing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class ItemsAdderUtil {
    public static boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
    }

    public static Optional<ItemStack> getIAItem(String id) {
        if (!isEnabled()) {
            BetterCallFishing.log(ChatColor.RED + "\""
                    + id + "\" this is an ItemsAdder item, but the ItemsAdder plugin " +
                    "is not loaded!");
            return Optional.empty();
        }

        String iaId = id.replace("IA:", "");
        if (CustomStack.isInRegistry(iaId)) {

            ItemStack item = CustomStack.getInstance(iaId).getItemStack().clone();

            return Optional.of(item);
        }

        BetterCallFishing.log(ChatColor.RED + "\""
                + id + "\" is not registered in ItemsAdder!");

        return Optional.empty();
    }
}
