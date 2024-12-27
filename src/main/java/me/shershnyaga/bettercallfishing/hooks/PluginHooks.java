package me.shershnyaga.bettercallfishing.hooks;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum PluginHooks {

    ITEMS_ADDER(new ItemsAdderHook()),
    MMO_ITEMS(new MMOItemsHook()),
    MYTHIC_MOBS(new MythicMobsHook());

    private final PluginHook hook;

    public boolean isEnabled() {
        return hook.isEnabled();
    }

    public String getHookName() {
        return hook.getHookName();
    }
}
