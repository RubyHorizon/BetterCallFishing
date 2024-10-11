package me.shershnyaga.bettercallfishing;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import me.shershnyaga.bettercallfishing.commands.BetterCallFishCmd;
import me.shershnyaga.bettercallfishing.config.BarrelConfig;
import me.shershnyaga.bettercallfishing.config.LangConfig;
import me.shershnyaga.bettercallfishing.config.WeightConfig;
import me.shershnyaga.bettercallfishing.events.OnFishEvent;
import me.shershnyaga.bettercallfishing.events.OtherEvents;
import me.shershnyaga.bettercallfishing.utils.AutoUpdate;
import me.shershnyaga.bettercallfishing.utils.Metrics;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class BetterCallFishing extends JavaPlugin {

    private static final boolean ENABLE_AUTO_UPDATE = true;

    public static final List<String> LANG_LIST = Arrays.asList(
            "ru",
            "eng",
            "de",
            "cz",
            "ua",
            "by",
            "zh_cn",
            "zh_hk",
            "zh_tw"
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

        if (ENABLE_AUTO_UPDATE) {
            Path dataFolderPath = Paths.get(this.getDataFolder().getAbsolutePath());
            Path pluginsFolderPath = dataFolderPath.getParent();
            new AutoUpdate(langConfig, getDescription().getVersion(), getConfig().getBoolean("auto-update"),
                    pluginsFolderPath.toAbsolutePath().toString(), null).update();
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        if (updateConfig()) {
            super.reloadConfig();
        }

        loadLang();
        weightConfig = new WeightConfig(getConfig(), langConfig);
        barrelConfig = new BarrelConfig(getConfig());
    }

    @SneakyThrows
    private void loadLang() {
        String langFolder = getDataFolder().getAbsolutePath() + File.separator + "lang" + File.separator;

        for (String lang: LANG_LIST) {
            if (!new File(getDataFolder().getAbsolutePath() + File.separator + "lang" + File.separator +
                    lang + ".yml").exists())
                saveResource("lang/" + lang + ".yml", false);
        }

        String langFileName = getConfig().getString("lang-file");

        if (!langFileName.endsWith(".yml")) {
            langFileName += ".yml";
        }

        InputStream in = getResource("lang/" + langFileName);

        if (in == null) {
            in = getResource("lang/eng.yml");
        }

        Reader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in),
                StandardCharsets.UTF_8));

        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);

        langConfig = new LangConfig(langFolder + langFileName, defaultConfig);
    }

    @SneakyThrows
    private boolean updateConfig() {
        boolean updated = false;

        InputStream in = getResource("config.yml");
        Reader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in),
                StandardCharsets.UTF_8));

        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);

        Set<String> keys = getConfig().getKeys(false);
        for (String key: defaultConfig.getKeys(false)) {
            if (!keys.contains(key) && !key.equals("barrel-items")) {
                updated = true;
                getConfig().set(key, defaultConfig.get(key));
            }
        }

        getConfig().save(new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml"));
        return updated;
    }

    private void reloadCommands() {
        Objects.requireNonNull(getServer().getPluginCommand("bettercallfishing"))
                .setExecutor(new BetterCallFishCmd(barrelConfig, reloadManager, langConfig));
    }

    private void reloadEvents() {
        if (isLoaded) {
            HandlerList.unregisterAll(this);
        }

        getServer().getPluginManager().registerEvents(new OnFishEvent(getConfig(), barrelConfig,
                new FixedMetadataValue(this, true), langConfig), this);
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