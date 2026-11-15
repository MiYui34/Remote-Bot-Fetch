package com.sguserver.remotebotfetch.client.config;

import com.google.common.collect.ImmutableList;
import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.annotations.Config;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigHotkey;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigsManager {
    public static final List<ConfigHotkey> KEY_LIST = new ArrayList<>();
    public static final List<IHotkeyTogglable> SWITCH_KEY = new ArrayList<>();
    private static final List<IConfigBase> ALL = new ArrayList<>();

    public static void init() {
        Arrays.stream(Configs.classes)
              .flatMap(clz -> Arrays.stream(clz.getDeclaredFields()))
              .filter(field -> field.isAnnotationPresent(Config.class))
              .forEach(ConfigsManager::addConfig);
    }

    public static void addConfig(Field field) {
        ConfigBase<?> obj;

        try {
            obj = (ConfigBase<?>) field.get(null);
        }
        catch (Exception e) {
            ClientMod.LOGGER.error("error while adding tab", e);
            return;
        }

        ALL.add(obj);

        if (obj instanceof ConfigHotkey hotkey) {
            KEY_LIST.add(hotkey);
        }
        else if (obj instanceof ConfigBooleanHotkeyed booleanHotkeyed) {
            SWITCH_KEY.add(booleanHotkeyed);
        }
    }

    public static ImmutableList<IConfigBase> getConfigs(Tab tab) {
        ImmutableList<IConfigBase> list = ImmutableList.copyOf(ALL);
        if (list.isEmpty())
            throw new IllegalStateException("Not initialized");
        return list;
    }
}
