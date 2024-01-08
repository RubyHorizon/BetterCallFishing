package xyz.sherhsnyaga.bettercallfishing.commands;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.sherhsnyaga.bettercallfishing.config.BarrelConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
public class BetterCallFishCmd implements TabExecutor {

    private BarrelConfig barrelConfig;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        if (!commandSender.isOp())
            if (commandSender.hasPermission("bettercallfish.*"))
                return true;

        Player player = (Player) commandSender;

        if (strings[0].equals("reload")) {
            commandSender.sendMessage("Doesn't work!");
        }
        else if (strings[0].equals("gen_barrel")) {
            HashMap<Integer, ItemStack> items = barrelConfig.generateBarrelInventoryMap();
            Inventory inv = Bukkit.createInventory(null, InventoryType.BARREL);
            items.forEach(inv::setItem);

            player.openInventory(inv);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String s, @NotNull String[] strings) {

        if (!commandSender.isOp())
            if (commandSender.hasPermission("bettercallfish.*"))
                return new ArrayList<>();

        if (strings.length < 2)
            return List.of("gen_barrel", "reload");

        return null;
    }
}
