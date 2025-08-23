package me.shershnyaga.bettercallfishing;

import lombok.Getter;
import lombok.SneakyThrows;
import me.shershnyaga.bettercallfishing.commands.BetterCallFishCmd;
import me.shershnyaga.bettercallfishing.config.BarrelConfig;
import me.shershnyaga.bettercallfishing.config.LangConfig;
import me.shershnyaga.bettercallfishing.config.MythicMobsConfig;
import me.shershnyaga.bettercallfishing.config.WeightConfig;
import me.shershnyaga.bettercallfishing.events.OnFishEvent;
import me.shershnyaga.bettercallfishing.events.OtherEvents;
import me.shershnyaga.bettercallfishing.utils.Metrics;
import me.shershnyaga.bettercallfishing.utils.integrations.ItemsAdderUtil;
import me.shershnyaga.bettercallfishing.utils.integrations.MythicMobsUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    private MythicMobsConfig mythicMobsConfig;

    private boolean isLoaded = false;
    private File barrelConfigFile;
    private File mythicConfigFile;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // reloadConfig();

        reloadManager = new ReloadManager();

        reloadManager.reload();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        if (updateConfig()) {
            super.reloadConfig();
        }

        displayAndDumpHooksConfigs();

        barrelConfigFile = new File(getDataFolder(), "barrel_config.yml");
        mythicConfigFile = new File(getDataFolder(), "mythic_mobs.yml");

        if (MythicMobsUtil.isEnabled()) {
            FileConfiguration mythicConfig = YamlConfiguration.loadConfiguration(mythicConfigFile);
            mythicMobsConfig = new MythicMobsConfig(mythicConfig);
        }

        loadLang();

        if (Files.notExists(Path.of(getDataFolder().getAbsolutePath() + File.separator + "barrel_config.yml"))
                && !moveBarrelConfig()) {
            saveResource("barrel_config.yml", false);
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(barrelConfigFile);

        weightConfig = new WeightConfig(getConfig(), langConfig);
        barrelConfig = new BarrelConfig(cfg);
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
                .setExecutor(new BetterCallFishCmd(barrelConfig, reloadManager, langConfig, mythicMobsConfig));
    }

    private void reloadEvents() {
        if (isLoaded) {
            HandlerList.unregisterAll(this);
        }

        getServer().getPluginManager().registerEvents(new OnFishEvent(getConfig(), barrelConfig, mythicMobsConfig,
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

    private boolean moveBarrelConfig() {

        if (getConfig().contains("barrel-items")) {
            File oldCofnigFile = new File(getDataFolder().getAbsolutePath(), "config.yml");

            try (FileWriter writer = new FileWriter(barrelConfigFile)) {
                writer.write("");
                writer.close();

                FileConfiguration oldConfig = getConfig();
                FileConfiguration newConfig = YamlConfiguration.loadConfiguration(barrelConfigFile);

                if (oldConfig.contains("barrel-items")) {
                    if (oldConfig.contains("enable-barrel-catch")) {
                        newConfig.set("enable-barrel-catch", oldConfig.get("enable-barrel-catch"));
                        oldConfig.set("enable-barrel-catch", null);
                    }

                    if (oldConfig.contains("barrel-catch-chance")) {
                        newConfig.set("barrel-catch-chance", oldConfig.get("barrel-catch-chance"));
                        oldConfig.set("barrel-catch-chance", null);
                    }

                    if (oldConfig.contains("barrel-items")) {
                        newConfig.set("barrel-items", oldConfig.getConfigurationSection("barrel-items"));
                        oldConfig.set("barrel-items", null);
                    }

                    try {
                        newConfig.save(barrelConfigFile);
                        oldConfig.save(oldCofnigFile);
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private void displayAndDumpHooksConfigs() {
        boolean isNone = true;

        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.GREEN + "Initializing Better Call Fishing Hooks: ");
        if (ItemsAdderUtil.isEnabled()) {
            isNone = false;
            builder.append(ChatColor.GREEN + "ItemsAdder, ");
        }

        if (MythicMobsUtil.isEnabled()) {
            if (Files.notExists(Path.of(getDataFolder().getAbsolutePath() + File.separator + "mythic_mobs.yml"))) {
                saveResource("mythic_mobs.yml", false);
            }

            isNone = false;
            builder.append(ChatColor.GREEN + "MythicMobs, ");
        }

        String message = builder.toString();
        if (message.endsWith(", ")) {
            message = message.substring(0, message.length() - 2);
        }

        if (!isNone) {
            BetterCallFishing.log(message);
        }
    }

    public static void log(String message) {
        if (!message.startsWith("[BetterCallFishing]")) {
            Bukkit.getConsoleSender().sendMessage("[BetterCallFishing] " + message);
        } else {
            Bukkit.getConsoleSender().sendMessage(message);
        }
    }

}
