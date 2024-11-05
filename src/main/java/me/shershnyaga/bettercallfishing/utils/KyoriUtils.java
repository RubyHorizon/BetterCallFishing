package me.shershnyaga.bettercallfishing.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

public final class KyoriUtils {

    private static MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static String getComponentAsString(Component component) {
        BaseComponent[] barrelName = BungeeComponentSerializer.get().serialize(component);
        return BaseComponent.toLegacyText(barrelName);
    }

    public static String translateMiniMessage(String message) {
        return getComponentAsString(MINI_MESSAGE.deserialize(message));
    }

}
