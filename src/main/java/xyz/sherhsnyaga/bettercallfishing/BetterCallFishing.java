package xyz.sherhsnyaga.bettercallfishing;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.sherhsnyaga.bettercallfishing.commands.BetterCallFishCmd;
import xyz.sherhsnyaga.bettercallfishing.config.BarrelConfig;
import xyz.sherhsnyaga.bettercallfishing.config.LangConfig;
import xyz.sherhsnyaga.bettercallfishing.config.WeightConfig;
import xyz.sherhsnyaga.bettercallfishing.events.OnFishEvent;
import xyz.sherhsnyaga.bettercallfishing.events.OtherEvents;
import xyz.sherhsnyaga.bettercallfishing.metrics.Metrics;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class BetterCallFishing extends JavaPlugin {
    public static final List<String> LANG_LIST = Arrays.asList(
            "ru",
            "eng"
    );

    @Getter
    private static ReloadManager reloadManager;
    private Metrics metrics;
    private BarrelConfig barrelConfig;
    private LangConfig langConfig;
    private WeightConfig weightConfig;

    private boolean isLoaded = false;

    @Override
    public void onEnable() {
        metrics = new Metrics(this, 20687);
        saveDefaultConfig();
        reloadConfig();

        reloadManager = new ReloadManager();
        reloadManager.reload();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        loadLang();
        weightConfig = new WeightConfig(getConfig(), langConfig);
        barrelConfig = new BarrelConfig(getConfig());
    }

    private void loadLang() {
        String langFolder = getDataFolder().getAbsolutePath() + File.separator + "lang" + File.separator;

        for (String lang: LANG_LIST)
            saveResource("lang/" + lang + ".yml", false);

        File langFile = new File(langFolder + getConfig().getString("lang-file"));
        langConfig = new LangConfig(YamlConfiguration.loadConfiguration(langFile));
    }

    private void reloadCommands() {
        Objects.requireNonNull(getServer().getPluginCommand("bettercallfishing"))
                .setExecutor(new BetterCallFishCmd(barrelConfig, reloadManager, langConfig));
    }

    private void reloadEvents() {
        if (isLoaded) {
            HandlerList.unregisterAll(this);
        }

        getServer().getPluginManager().registerEvents(new OnFishEvent(barrelConfig), this);
        getServer().getPluginManager().registerEvents(new OtherEvents(weightConfig), this);
    }

    public class ReloadManager {
        public void reload() {
            reloadConfig();
            reloadCommands();
            reloadEvents();
        }
    }
}
