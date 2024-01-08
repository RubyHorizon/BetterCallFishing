package xyz.sherhsnyaga.bettercallfishing.events;

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Barrel;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.sherhsnyaga.bettercallfishing.config.BarrelConfig;

import java.util.*;

@AllArgsConstructor
public class Events implements Listener {
    private final static NamespacedKey PERSISTENT_BARREL = NamespacedKey.fromString("bettercallfishing_barrel");

    private BarrelConfig barrelConfig;

    @EventHandler
    private void fishEvent(PlayerFishEvent event) {
        Location playerLoc = event.getPlayer().getLocation().add(0, 2, 0);
        Location hookLoc = event.getHook().getLocation();
        Location change = playerLoc.subtract(hookLoc);
        if (event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) {
            Entity fish = getFish(event.getCaught());

            if (fish == null)
                return;
            fish.setVelocity(change.toVector().multiply(0.15f));

            Objects.requireNonNull(event.getCaught()).remove();
        }
        else {
            if (new Random().nextInt(100) > 90) {
                Objects.requireNonNull(event.getCaught()).remove();

                if (barrelConfig.testBarrelCatch()) {
                    FallingBlock fallingBlock = hookLoc.getWorld()
                            .spawnFallingBlock(hookLoc, Material.BARREL.createBlockData());
                    fallingBlock.setDropItem(false);
                    fallingBlock.getPersistentDataContainer().set(
                            Objects.requireNonNull(PERSISTENT_BARREL),
                            PersistentDataType.INTEGER, 1
                    );
                }
            }
        }
    }

    @EventHandler
    private void onBarrelLand(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock fallingBlock) {
            if (!fallingBlock.getPersistentDataContainer().has(PERSISTENT_BARREL, PersistentDataType.INTEGER))
                return;

            if (event.getBlock() instanceof Barrel barrel) {
                HashMap<Integer, ItemStack> inv = barrelConfig.generateBarrelInventoryMap();

                inv.forEach((slot, item) -> barrel.getInventory().setItem(slot, item));
            }
        }
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
}
