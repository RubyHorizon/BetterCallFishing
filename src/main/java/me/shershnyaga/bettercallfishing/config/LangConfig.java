package me.shershnyaga.bettercallfishing.config;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
public class LangConfig {
    private Component reloadMessage;
    private List<String> updateDetectMessage;
    private Component updatedMessage;
    private Component oldBarrelName;

    public LangConfig(String configPath, YamlConfiguration defaultConfig) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(configPath));
        load(config, defaultConfig, configPath);
    }

    private void load(YamlConfiguration config, YamlConfiguration defaultConfig, String path) {
        MiniMessage miniMessage = MiniMessage.miniMessage();

        checkUpdatesOnFile(config, defaultConfig, path);

        reloadMessage = miniMessage.deserialize(Objects.requireNonNull(config.getString("reload-message")));
        updatedMessage = miniMessage.deserialize(Objects.requireNonNull(config.getString("updated-message")));
        updateDetectMessage = config.getStringList("update-detect-message");
        oldBarrelName = miniMessage.deserialize(Objects.requireNonNull(config.getString("old-barrel-name")));
    }

    @SneakyThrows
    private void checkUpdatesOnFile(YamlConfiguration config, YamlConfiguration defaultConfig, String path) {
        Set<String> keys = config.getKeys(false);
        defaultConfig.getKeys(false).forEach(key -> {
            if (!keys.contains(key)) {
                config.set(key, defaultConfig.get(key));
            }
        });

        config.save(new File(path));
    }
}
