package xyz.sherhsnyaga.bettercallfishing.config;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter
public class LangConfig {
    private String weightString;

    public LangConfig(YamlConfiguration config) {
        load(config);
    }

    public void load(YamlConfiguration config) {

    }

}
