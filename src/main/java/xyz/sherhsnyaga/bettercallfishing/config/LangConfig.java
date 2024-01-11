package xyz.sherhsnyaga.bettercallfishing.config;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Objects;

@Getter
public class LangConfig {
    private Component reloadMessage;
    public LangConfig(YamlConfiguration config) {
        load(config);
    }

    public void load(YamlConfiguration config) {
        MiniMessage miniMessage = MiniMessage.miniMessage();

        reloadMessage = miniMessage.deserialize(Objects.requireNonNull(config.getString("reload-message")));
    }

}
