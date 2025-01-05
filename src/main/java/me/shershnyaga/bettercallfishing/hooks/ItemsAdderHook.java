package me.shershnyaga.bettercallfishing.hooks;

import dev.lone.itemsadder.api.CustomStack;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.shershnyaga.bettercallfishing.BetterCallFishing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ItemsAdderHook implements PluginHook {
    @Override
    public String getHookName() {
        return "ItemsAdder";
    }

    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
    }

    public Optional<ItemStack> getItem(String id) {
        if (!isEnabled()) {
            BetterCallFishing.log(ChatColor.RED + "\""
                    + id + "\" this is an ItemsAdder item, but the ItemsAdder plugin " +
                    "is not loaded!");
            return Optional.empty();
        }

        String iaId = id.replace("IA:", "");
        iaId = iaId.replace("ia:", "");
        if (CustomStack.isInRegistry(iaId)) {

            ItemStack item = CustomStack.getInstance(iaId).getItemStack().clone();

            return Optional.of(item);
        }

        BetterCallFishing.log(ChatColor.RED + "\""
                + id + "\" is not registered in ItemsAdder!");

        return Optional.empty();
    }

    public Optional<ItemStack> getItem(String id, int count) {
        Optional<ItemStack> item = getItem(id);

        if (item.isPresent()) {
            ItemStack stack = item.get();
            stack.setAmount(count);
            return Optional.of(stack);
        }

        return item;
    }
}
