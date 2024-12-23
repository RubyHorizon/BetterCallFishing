package me.shershnyaga.bettercallfishing.commands;

import lombok.AllArgsConstructor;
import me.shershnyaga.bettercallfishing.BetterCallFishing;
import me.shershnyaga.bettercallfishing.config.BarrelConfig;
import me.shershnyaga.bettercallfishing.config.LangConfig;
import me.shershnyaga.bettercallfishing.config.MythicMobsConfig;
import me.shershnyaga.bettercallfishing.utils.integrations.ItemsAdderUtil;
import me.shershnyaga.bettercallfishing.utils.integrations.MythicMobsUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
public class BetterCallFishCmd implements TabExecutor {
    private BarrelConfig barrelConfig;
    private BetterCallFishing.ReloadManager reloadManager;
    private LangConfig langConfig;
    private MythicMobsConfig mythicMobsConfig;
    private BukkitAudiences audiences;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {

        if (!commandSender.isOp()) {
            if (commandSender.hasPermission("bettercallfish.*")) {
                return true;
            }
        }

        if (strings[0].equals("reload") && commandSender.hasPermission("bettercallfishing.reload")) {
            reloadManager.reload();
            audiences.sender(commandSender).sendMessage(langConfig.getReloadMessage());
        }
        else if (strings[0].equals("gen_barrel") && commandSender.hasPermission("bettercallfishing.barrels")) {

            if (commandSender instanceof Player player) {
                HashMap<Integer, ItemStack> items = barrelConfig.generateBarrelInventoryMap();

                BaseComponent[] barrelName = BungeeComponentSerializer.get().serialize(langConfig.getOldBarrelName());

                Inventory inv = Bukkit.createInventory(null, InventoryType.BARREL,
                        BaseComponent.toLegacyText(barrelName));
                items.forEach(inv::setItem);

                player.openInventory(inv);
            }
        }
        else if (strings[0].equals("check") && strings.length == 2) {
            List<String> checks = getIntegrations();
            String action = strings[1];

            if (!checks.contains(action)) {
                return true;
            }

            switch (action) {
                case "integration_items_adder_barrel": {
                    if (ItemsAdderUtil.isEnabled()) {
                        getAvailableItemsOnItemsAdder().forEach(commandSender::sendMessage);
                    }
                }
                case "integration_mythicmobs": {
                    if (MythicMobsUtil.isEnabled()) {
                        getAvailableMobsOnMythicMobs().forEach(commandSender::sendMessage);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String s, @NotNull String[] strings) {

        if (!commandSender.isOp()) {
            if (commandSender.hasPermission("bettercallfish.*")) {
                return new ArrayList<>();
            }
        }

        List<String> checks = getIntegrations();

        if (strings.length == 1) {

            List<String> args = new ArrayList<>();
            args.add("reload");

            if (commandSender instanceof Player) {
                args.add("gen_barrel");
            }

            if (!checks.isEmpty()) {
                args.add("check");
            }

            return args;
        }

        if (strings.length == 2 && !checks.isEmpty() && strings[0].equals("check")) {
            return checks;
        }

        return null;
    }

    private List<String> getIntegrations() {
        List<String> checks = new ArrayList<>();

        if (ItemsAdderUtil.isEnabled()) {
            checks.add("integration_items_adder_barrel");
        }

        if (MythicMobsUtil.isEnabled()) {
            checks.add("integration_mythicmobs");
        }

        return checks;
    }

    private List<String> getAvailableItemsOnItemsAdder() {
        List<String> answer = new ArrayList<>();
        answer.add(ChatColor.GOLD + "------------");
        answer.add("Loaded ItemsAdder items in BetterCallFishing: ");

        StringBuilder loaded = new StringBuilder();
        StringBuilder unloaded = new StringBuilder();

        for (BarrelConfig.ItemSettings item: barrelConfig.getItemSettingsList()) {

            if (!item.isIAItem()) {
                continue;
            }

            if (item.isLoadedIAItem()) {
                loaded.append(ChatColor.GREEN).append(item.getId()).append(ChatColor.WHITE).append(", ");
            }
            else {
                unloaded.append(ChatColor.RED).append(item.getId()).append(ChatColor.WHITE).append(", ");
            }
        }

        String loadedAsString = loaded.toString();
        String unloadedAsString = unloaded.toString();
        if (loadedAsString.endsWith(", ")) {
            loadedAsString = loadedAsString.substring(0, loadedAsString.length() - 2);
        }

        if (unloadedAsString.endsWith(", ")) {
            unloadedAsString = unloadedAsString.substring(0, unloadedAsString.length() - 2);
        }

        answer.add(loadedAsString);
        answer.add(" ");
        answer.add("Unloaded ItemsAdder items in BetterCallFishing: ");

        answer.add(unloadedAsString);
        answer.add(ChatColor.GOLD + "------------");
        return answer;
    }

    private List<String> getAvailableMobsOnMythicMobs() {
        List<String> answer = new ArrayList<>();
        answer.add(ChatColor.GOLD + "------------");
        answer.add("Loaded MythicMobs in BetterCallFishing: ");

        StringBuilder loaded = new StringBuilder();
        StringBuilder unloaded = new StringBuilder();

        for (MythicMobsConfig.MythicMobInfo mob: mythicMobsConfig.getMobs()) {
            if (mob.isLoaded()) {
                loaded.append(ChatColor.GREEN).append(mob.getId()).append(ChatColor.WHITE).append(", ");
            }
            else {
                unloaded.append(ChatColor.RED).append(mob.getId()).append(ChatColor.WHITE).append(", ");
            }
        }

        String loadedAsString = loaded.toString();
        String unloadedAsString = unloaded.toString();
        if (loadedAsString.endsWith(", ")) {
            loadedAsString = loadedAsString.substring(0, loadedAsString.length() - 2);
        }

        if (unloadedAsString.endsWith(", ")) {
            unloadedAsString = unloadedAsString.substring(0, unloadedAsString.length() - 2);
        }

        answer.add(loadedAsString);

        answer.add(" ");

        answer.add("Unloaded MythicMobs in BetterCallFishing: ");

        answer.add(unloadedAsString);
        answer.add(ChatColor.GOLD + "------------");
        return answer;
    }
}
