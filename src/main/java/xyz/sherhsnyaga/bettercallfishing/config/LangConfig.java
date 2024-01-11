package xyz.sherhsnyaga.bettercallfishing.config;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Objects;

@Getter
public class LangConfig {
    private Component reloadMessage;
    private List<String> updateDetectMessage;
    private Component updatedMessage;
    public LangConfig(YamlConfiguration config) {
        load(config);
    }

    public void load(YamlConfiguration config) {
        MiniMessage miniMessage = MiniMessage.miniMessage();

        reloadMessage = miniMessage.deserialize(Objects.requireNonNull(config.getString("reload-message")));
        updatedMessage = miniMessage.deserialize(Objects.requireNonNull(config.getString("updated-message")));
        updateDetectMessage = config.getStringList("update-detect-message");
    }

}
