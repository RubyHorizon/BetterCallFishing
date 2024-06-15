package me.sherhsnyaga.bettercallfishing.utils;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import me.sherhsnyaga.bettercallfishing.config.LangConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class AutoUpdate {
    private static final String UPDATE_URL = "https://shershnyaga.me/plugins/bettercallfishing.yml";
    private LangConfig langConfig;
    private String version;
    private boolean enabled;
    private String pluginsFolder;
    private UpdateInfo bufferedInfo;

    public boolean checkUpdates() {
        UpdateInfo info = getUpdateInfo();
        bufferedInfo = info;
        if (info == null)
            return false;

        return !info.version.equals(version);
    }

    public void update() {
        if (!checkUpdates()) {
            return;
        }

        if (bufferedInfo == null) {
            return;
        }

        sendMessages();

        if (enabled)
            downloadPlugin();
    }

    @SneakyThrows
    private void downloadPlugin() {
        Files.walk(Paths.get(pluginsFolder))
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase().contains("bettercallfishing"))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        URL url = new URL(bufferedInfo.updateUrl);
        String[] splited = bufferedInfo.updateUrl.split("/");
        String fileName = splited[splited.length - 1];

        try (InputStream in = url.openStream()) {
            Files.copy(in, Path.of(pluginsFolder + File.separator + fileName),
                    StandardCopyOption.REPLACE_EXISTING);

            LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().hexColors().build();
            Bukkit.getLogger().info(serializer.serialize(langConfig.getUpdatedMessage()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessages() {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        List<String> messages = langConfig.getUpdateDetectMessage();
        List<Component> messageAsComponent = new ArrayList<>();

        for (String message: messages) {
            if (message.contains("%version_info%")) {
                for (String changes: bufferedInfo.changes) {
                    messageAsComponent.add(miniMessage.deserialize(changes));
                }
                continue;
            }

            message = message.replace("%new_version%", bufferedInfo.version);
            message = message.replace("%old_version%", version);

            messageAsComponent.add(miniMessage.deserialize(message));
        }

        LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().hexColors().build();
        for (Component component: messageAsComponent)
            Bukkit.getLogger().info(serializer.serialize(component));
    }

    private UpdateInfo getUpdateInfo() {
        try {
            URL url = new URL(UPDATE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                        "utf8"));
                YamlConfiguration configuration  = YamlConfiguration.loadConfiguration(reader);
                String version = configuration.getString("version");
                String updateUrl = configuration.getString("update-url");
                List<String> changes = configuration.getStringList("changes");
                connection.disconnect();
                return new UpdateInfo(version, updateUrl, changes);
            }
            else {
                connection.disconnect();
                return null;
            }
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "[BetterCallFishing] Failed to check updates:");
            e.printStackTrace();
        }

        return null;
    }

    @AllArgsConstructor
    private class UpdateInfo {
        private String version;
        private String updateUrl;
        private List<String> changes;
    }
}
