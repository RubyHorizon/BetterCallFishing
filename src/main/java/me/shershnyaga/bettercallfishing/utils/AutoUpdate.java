package me.shershnyaga.bettercallfishing.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.shershnyaga.bettercallfishing.BetterCallFishing;
import me.shershnyaga.bettercallfishing.config.LangConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AutoUpdate {
    private static final String UPDATE_URL = "https://shershnyaga.me/plugins/bettercallfishing.yml";

    @NonNull
    private LangConfig langConfig;

    @NonNull
    private String currentVersion;

    @NonNull
    private boolean enabled;

    @NonNull
    private String pluginsFolder;

    private UpdateInfo bufferedInfo;

    @Getter
    private boolean updateFound = false;

    public boolean checkUpdates() {
        UpdateInfo info = getUpdateInfo();
        bufferedInfo = info;
        if (info == null) {
            return false;
        }

        return !info.version.equals(currentVersion);
    }

    public void update() {
        if (!checkUpdates()) {
            return;
        }

        if (bufferedInfo == null) {
            return;
        }

        updateFound = true;

        sendMessages();

        if (enabled) {
            downloadPlugin();
        }
    }

    private void downloadPlugin() {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            URL url = new URL(bufferedInfo.updateUrl);

            String[] splited = bufferedInfo.updateUrl.split("/");
            String fileName = splited[splited.length - 1];

            try (InputStream in = url.openStream()) {
                Files.copy(in, Path.of(pluginsFolder + File.separator + fileName),
                        StandardCopyOption.REPLACE_EXISTING);

                LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().hexColors().build();
                BetterCallFishing.log(serializer.serialize(langConfig.getUpdatedMessage()));
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException ignored) {

        }
    }

    private void sendMessages() {
        for (String message: getUpdateMessage()) {
            BetterCallFishing.log(message);
        }
    }

    public List<String> getUpdateMessage() {
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
            message = message.replace("%old_version%", currentVersion);
            message = message.replace("%current_version%", currentVersion);

            messageAsComponent.add(miniMessage.deserialize(message));
        }

        List<String> mess = new ArrayList<>();
        for (Component component: messageAsComponent) {
            BaseComponent[] name = BungeeComponentSerializer.get().serialize(component);
            mess.add(BaseComponent.toLegacyText(name));
        }

        return mess;
    }

    private UpdateInfo getUpdateInfo() {
        try {
            URL url = new URL(UPDATE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setUseCaches(false);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                        "utf8"));
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
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
            BetterCallFishing.log(ChatColor.RED + "Failed to check updates. Please try again " +
                    "later.");
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
