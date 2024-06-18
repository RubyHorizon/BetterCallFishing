package me.sherhsnyaga.bettercallfishing;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import me.sherhsnyaga.bettercallfishing.commands.BetterCallFishCmd;
import me.sherhsnyaga.bettercallfishing.config.BarrelConfig;
import me.sherhsnyaga.bettercallfishing.config.LangConfig;
import me.sherhsnyaga.bettercallfishing.config.WeightConfig;
import me.sherhsnyaga.bettercallfishing.events.OnFishEvent;
import me.sherhsnyaga.bettercallfishing.events.OtherEvents;
import me.sherhsnyaga.bettercallfishing.utils.AutoUpdate;
import me.sherhsnyaga.bettercallfishing.utils.Metrics;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class BetterCallFishing extends JavaPlugin {
    public static final List<String> LANG_LIST = Arrays.asList(
            "ru",
            "eng",
            "de"
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
        saveDefaultConfig();
        reloadConfig();

        reloadManager = new ReloadManager();
        reloadManager.reload();

        Path dataFolderPath = Paths.get(this.getDataFolder().getAbsolutePath());
        Path pluginsFolderPath = dataFolderPath.getParent();
        new AutoUpdate(langConfig, getDescription().getVersion(), getConfig().getBoolean("auto-update"),
                pluginsFolderPath.toAbsolutePath().toString(),null).update();
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

        for (String lang: LANG_LIST) {
            if (!new File(getDataFolder().getAbsolutePath() + File.separator + "lang" + File.separator +
                    lang + ".yml").exists())
                saveResource("lang/" + lang + ".yml", false);
        }

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

        getServer().getPluginManager().registerEvents(new OnFishEvent(barrelConfig,
                new FixedMetadataValue(this, true)), this);
        getServer().getPluginManager().registerEvents(new OtherEvents(weightConfig), this);
    }

    private void setupMetrics() {
        metrics = new Metrics(this, 20687);
        metrics.addCustomChart(new Metrics.SimplePie("used_language", () ->
                Objects.requireNonNull(getConfig().getString("lang-file")).replace(".yml", "")));
        metrics.addCustomChart(new Metrics.SimplePie("used_auto_update", () ->
                Objects.requireNonNull(getConfig().getString("auto-update"))));
    }

    public class ReloadManager {
        public void reload() {
            reloadConfig();
            reloadCommands();
            reloadEvents();
            setupMetrics();
            isLoaded = true;
        }
    }
}
