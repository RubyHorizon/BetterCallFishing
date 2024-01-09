package xyz.sherhsnyaga.bettercallfishing.events;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Barrel;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import xyz.sherhsnyaga.bettercallfishing.config.BarrelConfig;

import java.util.*;

@AllArgsConstructor
public class OnFishEvent implements Listener {
    private BarrelConfig barrelConfig;

    @EventHandler
    private void fishEvent(PlayerFishEvent event) {
        Location playerLoc = event.getPlayer().getLocation().add(0, 2, 0);
        Entity caught = event.getCaught();
        Location hookLoc = event.getHook().getLocation();
        Location change = playerLoc.subtract(hookLoc);

        if (event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) {

            if (new Random().nextInt(10000) == 9999) {
                Entity stalin = spawnStalin(hookLoc);
                stalin.setVelocity(caught.getVelocity().multiply(3));
                caught.remove();
                return;
            }

            Entity fish = getFish(event.getCaught());

            if (fish != null) {
                fish.setVelocity(change.toVector().multiply(0.15f));
                Objects.requireNonNull(event.getCaught()).remove();
                fish.getPersistentDataContainer().set(NamespacedKey.fromString("hook_time"),
                        PersistentDataType.LONG, System.currentTimeMillis()
                        );
            }
            else {
                if (barrelConfig.testBarrelCatch()) {
                    spawnBarrel(caught.getLocation(), caught.getVelocity());
                    caught.remove();
                }
            }
        }
    }

    private void spawnBarrel(Location loc, Vector velocity) {
        HashMap<Integer, ItemStack> items = barrelConfig.generateBarrelInventoryMap();

        ItemStack item = new ItemStack(Material.BARREL);
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        Barrel barrel = (Barrel) meta.getBlockState();
        Inventory inv = barrel.getInventory();
        items.forEach(inv::setItem);
        meta.setBlockState(barrel);
        item.setItemMeta(meta);

        Entity e = loc.getWorld().dropItem(loc, item);
        e.setVelocity(velocity);
    }

    private Entity getFish(Entity entity) {
        Item item = (Item) entity;

        ItemStack itemStack = Objects.requireNonNull(item).getItemStack();

        if (itemStack.getType() == Material.COD) {
            return entity.getWorld().spawnEntity(entity.getLocation(), EntityType.COD);
        }
        else if (itemStack.getType() == Material.SALMON) {
            return entity.getWorld().spawnEntity(entity.getLocation(), EntityType.SALMON);
        }
        else if (itemStack.getType() == Material.PUFFERFISH) {
            return entity.getWorld().spawnEntity(entity.getLocation(), EntityType.PUFFERFISH);
        }
        else if (itemStack.getType() == Material.TROPICAL_FISH) {
            Entity fish = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.TROPICAL_FISH);
            TropicalFish tropicalFish = (TropicalFish) fish;
            tropicalFish.setPattern(getRandomTropicalFishPattern());

            return tropicalFish;

        }
        else {
            return null;
        }

    }

    private TropicalFish.Pattern getRandomTropicalFishPattern() {
        List<TropicalFish.Pattern> colors = new ArrayList<>();
        Collections.addAll(colors, TropicalFish.Pattern.values());
        Random random = new Random();
        int size = colors.size() - 1;
        return colors.get(random.nextInt(size));
    }

    private Entity spawnStalin(Location loc) {
        Giant giant = (Giant) loc.getWorld().spawnEntity(loc, EntityType.GIANT);

        giant.customName(Component.text(ChatColor.RED + "Stalin"));
        giant.setAI(true);
        return giant;
    }
}
