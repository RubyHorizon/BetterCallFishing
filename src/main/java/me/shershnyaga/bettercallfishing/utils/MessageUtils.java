package me.shershnyaga.bettercallfishing.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;

public final class MessageUtils {

    public static String convertComponentToString(Component component) {
        BaseComponent[] baseComponent = BungeeComponentSerializer.get().serialize(component);
        return BaseComponent.toLegacyText(baseComponent);
    }

    public static String parseChatColors(String string) {
        MiniMessage miniMessage = MiniMessage.miniMessage();

        String parsedAlternative = parseAlternativeChatColors(string);

        return convertComponentToString(miniMessage.deserialize(parsedAlternative));
    }

    public static String parseAlternativeChatColors(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
