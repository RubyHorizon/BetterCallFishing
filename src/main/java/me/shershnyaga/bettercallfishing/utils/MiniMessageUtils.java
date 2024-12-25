package me.shershnyaga.bettercallfishing.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

public final class MiniMessageUtils {

    public static String convertComponentToString(Component component) {
        BaseComponent[] baseComponent = BungeeComponentSerializer.get().serialize(component);
        return BaseComponent.toLegacyText(baseComponent);
    }
}
