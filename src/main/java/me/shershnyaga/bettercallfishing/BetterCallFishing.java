package me.shershnyaga.bettercallfishing;

import lombok.Getter;
import lombok.SneakyThrows;
import me.shershnyaga.bettercallfishing.events.OnJoinEvent;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import me.shershnyaga.bettercallfishing.commands.BetterCallFishCmd;
import me.shershnyaga.bettercallfishing.config.old.BarrelConfigOld;
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
    private BarrelConfigOld barrelConfigOld;
    private LangConfig langConfig;
    private WeightConfig weightConfig;

    private boolean isLoaded = false;

    private BukkitAudiences adventure;

    private AutoUpdate autoUpdate;

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);
        saveDefaultConfig();
        reloadConfig();

        Bukkit.getScheduler().runTaskAsynchronously(this, this::update);

        reloadManager = new ReloadManager();

        reloadManager.reload();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        if (updateConfig()) {
            super.reloadConfig();
        }

        loadLang();
        weightConfig = new WeightConfig(getConfig(), langConfig);
        barrelConfigOld = new BarrelConfigOld(getConfig());
    }

    @SneakyThrows
    private void loadLang() {
        String langFolder = getDataFolder().getAbsolutePath() + File.separator + "lang" + File.separator;

        for (String lang: LANG_LIST) {
            if (!new File(getDataFolder().getAbsolutePath() + File.separator + "lang" + File.separator +
                    lang + ".yml").exists()) {
                saveResource("lang/" + lang + ".yml", false);
            }
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
                .setExecutor(new BetterCallFishCmd(barrelConfigOld, reloadManager, langConfig, adventure));
    }

    private void reloadEvents() {
        if (isLoaded) {
            HandlerList.unregisterAll(this);
        }

        getServer().getPluginManager().registerEvents(new OnFishEvent(getConfig(), barrelConfigOld,
                new FixedMetadataValue(this, true), langConfig), this);
        getServer().getPluginManager().registerEvents(new OtherEvents(weightConfig), this);
        getServer().getPluginManager().registerEvents(new OnJoinEvent(autoUpdate), this);
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

    private void update() {
        if (ENABLE_AUTO_UPDATE) {

            String os = System.getProperty("os.name");

            if (os.toLowerCase().contains("windows")) {
                getLogger().info(ChatColor.YELLOW + "Automatic updates are unavailable in Windows. You can manually download the update " +
                        "using the following links:");
                getLogger().info(ChatColor.YELLOW + "https://www.spigotmc.org/resources/bettercallfishing.108426/");
                getLogger().info(ChatColor.YELLOW + "https://modrinth.com/plugin/bettercallfishing");
                return;
            }

            Path dataFolderPath = Paths.get(this.getDataFolder().getAbsolutePath());
            Path pluginsFolderPath = dataFolderPath.getParent();
            autoUpdate = new AutoUpdate(langConfig, getDescription().getVersion(), getConfig().getBoolean("auto-update"),
                    pluginsFolderPath.toAbsolutePath().toString());

            autoUpdate.update();
        }

    }

    @Override
    public void onDisable() {
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }
}
