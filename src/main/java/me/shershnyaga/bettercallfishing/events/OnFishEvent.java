package me.shershnyaga.bettercallfishing.events;

import lombok.AllArgsConstructor;
import me.shershnyaga.bettercallfishing.config.LangConfig;
import me.shershnyaga.bettercallfishing.utils.Constants;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import me.shershnyaga.bettercallfishing.config.BarrelConfig;

import java.util.*;

@AllArgsConstructor
public class OnFishEvent implements Listener {

    private final FileConfiguration config;
    private final BarrelConfig barrelConfig;
    private final FixedMetadataValue metadataValue;
    private final LangConfig langConfig;

    @EventHandler(priority = EventPriority.LOWEST)
    private void fishEvent(PlayerFishEvent event) {
        Location playerLoc = event.getPlayer().getLocation().add(0, 2, 0);
        Entity caught = event.getCaught();
        Location hookLoc = event.getHook().getLocation();
        Location change = playerLoc.subtract(hookLoc);

        if (event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) {

            Entity dolphin = tryToCatchDolphin(hookLoc);
            if (dolphin != null) {
                dolphin.setVelocity(caught.getVelocity().multiply(3));
                caught.remove();
                return;
            }

            Entity fish = getFish(event.getCaught());

            if (barrelConfig.testBarrelCatch()) {
                ItemStack barrel = getBarrelItem();
                Item caughtItem = (Item) caught;
                caughtItem.setItemStack(barrel);
                return;
            }

            if (fish != null) {
                fish.setVelocity(change.toVector().multiply(0.15f));
                Objects.requireNonNull(event.getCaught()).remove();
                fish.getPersistentDataContainer().set(Constants.HOOK_TIME_NAMESPACE,
                        PersistentDataType.LONG, System.currentTimeMillis()
                );
            }
        }
    }

    private FallingBlock spawnBarrelAsEntity(Location loc) {
        FallingBlock fallingBlock = loc.getWorld().spawnFallingBlock(loc, Material.BARREL.createBlockData());
        fallingBlock.setDropItem(true);
        fallingBlock.setHurtEntities(true);
        fallingBlock.setMetadata("bcf_loot", metadataValue);
        return fallingBlock;
    }

    private ItemStack getBarrelItem() {
        HashMap<Integer, ItemStack> items = barrelConfig.generateBarrelInventoryMap();

        ItemStack item = new ItemStack(Material.BARREL);
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();

        BaseComponent[] barrelName = BungeeComponentSerializer.get().serialize(langConfig.getOldBarrelName());

        meta.setDisplayName(BaseComponent.toPlainText(barrelName));
        Barrel barrel = (Barrel) meta.getBlockState();
        Inventory inv = barrel.getInventory();
        items.forEach(inv::setItem);
        meta.setBlockState(barrel);
        item.setItemMeta(meta);

        return item;
    }

    public Entity tryToCatchDolphin(Location loc) {
        if (new Random().nextFloat(100) < config.getDouble("dolphin-catch-chance")) {
            return loc.getWorld().spawnEntity(loc, EntityType.DOLPHIN);
        }

        return null;
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
