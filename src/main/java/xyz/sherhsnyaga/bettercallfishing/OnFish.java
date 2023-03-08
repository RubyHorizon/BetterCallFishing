package xyz.sherhsnyaga.bettercallfishing;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class OnFish implements Listener {

    @EventHandler
    private void fishEvent(PlayerFishEvent event) {
        if (event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) {
            Entity fish = getFish(event.getCaught());

            if (fish == null)
                return;

            Location playerLoc = event.getPlayer().getLocation().add(0, 2, 0);
            Location hookLoc = event.getHook().getLocation();
            Location change = playerLoc.subtract(hookLoc);
            fish.setVelocity(change.toVector().multiply(0.15f));

            Objects.requireNonNull(event.getCaught()).remove();
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
        return colors.get(random.nextInt(0, size));
    }
}
