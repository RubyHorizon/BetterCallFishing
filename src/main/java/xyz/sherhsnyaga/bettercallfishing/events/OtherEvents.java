package xyz.sherhsnyaga.bettercallfishing.events;

import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.sherhsnyaga.bettercallfishing.config.WeightConfig;

@AllArgsConstructor
public class OtherEvents implements Listener {
    private WeightConfig weightConfig;
    @EventHandler
    private void onRightClickAtFish(PlayerInteractAtEntityEvent event) {
        Entity fish = event.getRightClicked();
        if (!isFish(fish))
            return;

        if (!fish.getPersistentDataContainer().has(NamespacedKey.fromString("hook_time"), PersistentDataType.LONG))
            return;

        long time = fish.getPersistentDataContainer().get(NamespacedKey.fromString("hook_time"),
                PersistentDataType.LONG);

        if (System.currentTimeMillis() > time + 10000)
            return;

        ItemStack fishItem = getFishItem(fish);

        if (fishItem != null) {
            event.getPlayer().getInventory().addItem(fishItem);
            fish.remove();
        }
    }

    private boolean isFish(Entity entity) {
        return entity.getType() == EntityType.COD ||
                entity.getType() == EntityType.SALMON ||
                entity.getType() == EntityType.PUFFERFISH ||
                entity.getType() == EntityType.TROPICAL_FISH;
    }

    private ItemStack getFishItem(Entity entity) {
        ItemStack fish = null;
        if (entity.getType() == EntityType.COD) {
            fish = new ItemStack(Material.COD);
        }
        else if (entity.getType() == EntityType.SALMON) {
            fish = new ItemStack(Material.SALMON);
        }
        else if (entity.getType() == EntityType.PUFFERFISH) {
            fish = new ItemStack(Material.PUFFERFISH);
        }
        else if (entity.getType() == EntityType.TROPICAL_FISH) {
            fish = new ItemStack(Material.TROPICAL_FISH);
        }

        if (fish != null)
            fish = weightConfig.genWeight(fish);

        return fish;
    }

}
