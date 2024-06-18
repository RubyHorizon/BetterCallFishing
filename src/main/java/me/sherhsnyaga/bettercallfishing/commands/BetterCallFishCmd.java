package me.sherhsnyaga.bettercallfishing.commands;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.sherhsnyaga.bettercallfishing.BetterCallFishing;
import me.sherhsnyaga.bettercallfishing.config.BarrelConfig;
import me.sherhsnyaga.bettercallfishing.config.LangConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
public class BetterCallFishCmd implements TabExecutor {
    private BarrelConfig barrelConfig;
    private BetterCallFishing.ReloadManager reloadManager;
    private LangConfig langConfig;

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
            commandSender.sendMessage(langConfig.getReloadMessage());
        }
        else if (strings[0].equals("gen_barrel") && commandSender.hasPermission("bettercallfishing.barrels")) {

            if (commandSender instanceof Player player) {
                HashMap<Integer, ItemStack> items = barrelConfig.generateBarrelInventoryMap();
                Inventory inv = Bukkit.createInventory(null, InventoryType.BARREL);
                items.forEach(inv::setItem);

                player.openInventory(inv);
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

        if (strings.length == 1) {

            List<String> args = new ArrayList<>();
            args.add("reload");

            if (commandSender instanceof Player) {
                args.add("gen_barrel");
            }

            return args;
        }

        return null;
    }
}
