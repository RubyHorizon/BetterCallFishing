package me.shershnyaga.bettercallfishing.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class WeightConfig {
    private static final List<String> FISH_TYPES = List.of(
            "COD",
            "SALMON",
            "PUFFERFISH",
            "TROPICAL_FISH"
    );
    private final Random random;
    private final MiniMessage miniMessage;

    @Getter
    private boolean isWeightEnabled;
    private final HashMap<Material, FishSettings> fishSettings;
    private final LangConfig langConfig;

    public WeightConfig(FileConfiguration config, LangConfig langConfig) {
        fishSettings = new HashMap<>();
        random = new Random();
        miniMessage = MiniMessage.builder().build();
        this.langConfig = langConfig;
        setConfiguration(config);
    }

    public void setConfiguration(FileConfiguration config) {
        isWeightEnabled = config.getBoolean("weight.enable");

        for (String type: FISH_TYPES) {
            double minWeight = config.getDouble("weight." + type + ".min");
            double maxWeight = config.getDouble("weight." + type + ".max");
            fishSettings.put(Material.matchMaterial(type), new FishSettings(minWeight, maxWeight));
        }
    }

    public ItemStack genWeight(ItemStack itemStack) {
        if (itemStack.getAmount() == 1) {
            if (!fishSettings.containsKey(itemStack.getType())) {
                return itemStack;
            }

            FishSettings settings = fishSettings.get(itemStack.getType());

            double weight = getRandom(settings.minWeight, settings.maxWeight);
            String formattedWeight = String.format("%.3f", weight);

            // String nameStr = langConfig.getWeightString();
            // nameStr = nameStr.replace("%weight%", formattedWeight);
            ItemMeta meta = itemStack.getItemMeta();
            // meta.lore(List.of(miniMessage.deserialize(nameStr)));
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    private double getRandom(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    @AllArgsConstructor
    private class FishSettings {
        private double minWeight;
        private double maxWeight;
    }
}
