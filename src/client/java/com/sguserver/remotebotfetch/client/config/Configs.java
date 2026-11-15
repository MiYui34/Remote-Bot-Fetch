package com.sguserver.remotebotfetch.client.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.annotations.Config;
import com.sguserver.remotebotfetch.client.config.options.OpenFakePlayerInvMode;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
//#if MC>=260101
import fi.dy.masa.malilib.util.data.json.JsonUtils;
//#else
//$$ import fi.dy.masa.malilib.util.JsonUtils;
//#endif

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class Configs implements IConfigHandler {
    public static final Configs INSTANCE = new Configs();
    public static final String ALL = ClientMod.MOD_ID + ".config.all";
    public static final Class<?>[] classes = new Class[]{LMS.class, Lists.class, Configs.class};

    /**
     * Sets the translation prefix of a config option. 1.21.1's malilib (0.21.0) predates
     * {@code ConfigBase#apply(String)} and only knows the plain translated-name setter, so the
     * full {@code <prefix>.name.<option>} key is built by hand there. The config gui labels stay
     * the same on every version that way.
     */
    private static <T extends IConfigBase> T lang(T config, String prefix) {
        ConfigBase<?> base = (ConfigBase<?>) config;
//#if MC>=12103
        base.apply(prefix);
//#else
        //$$ base.translatedName(prefix + ".name." + config.getName());
//#endif
        return config;
    }

    public static class LMS {
        @Config(tab = Tab.ALL) public static final ConfigHotkey LMS_TAKE_ITEM = lang(new ConfigHotkey("openFakeFetchGui", "", ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigBoolean INVENTORY_FETCH_MENU = lang(new ConfigBoolean("inventoryFetchMenu", true, ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigBoolean LMS_FETCH_SUPPORT = lang(new ConfigBoolean("fakeFetchSupport", false, ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigBoolean LMS_AUTO_REFRESH = lang(new ConfigBoolean("fakeAutoRefresh", false, ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigBooleanHotkeyed MID_CLICK_TAKE_ITEM = lang(new ConfigBooleanHotkeyed("middleClickLitematicBlockFetch", false, "", ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigBooleanHotkeyed AUTO_OPEN_FAKE_PLAYER_INV = lang(new ConfigBooleanHotkeyed("autoOpenFakeInv", false, "", ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigOptionList AUTO_OPEN_FAKE_PLAYER_INV_MODE = lang(new ConfigOptionList("autoOpenFakeInv-Mode", OpenFakePlayerInvMode.INTERACTION, "", ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigBooleanHotkeyed AUTO_KILL_FAKE_PLAYERS = lang(new ConfigBooleanHotkeyed("autoKillFake", false, "", ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigInteger AUTO_COOLDOWN = lang(new ConfigInteger("autoActionCooldown", 100, 100, 1000, "", ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigBooleanHotkeyed FAKE_PLAYER_INVENTORY_ITEM_CACHE = lang(new ConfigBooleanHotkeyed("fakeInvCache", false, "", ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigHotkey CLEAN_PLAYER_INV_CHACHE = lang(new ConfigHotkey("fakeInvCache-clear", "", "", ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigBooleanHotkeyed LITEMATICA_CALCULATE_QWP = lang(new ConfigBooleanHotkeyed("MaterialList-CaculateLMS", false, "", ""), ALL);
        @Config(tab = Tab.ALL) public static final ConfigBooleanHotkeyed LITEMATICA_CALCULATE_FAKE = lang(new ConfigBooleanHotkeyed("MaterialList-CaculateInv", false, "", ""), ALL);
    }

    public static class Lists {
        @Config(tab = Tab.ALL) public static final ConfigStringList FAKE_PLAYER_INVENTORY_ITEM_CACHE_WHITE_LIST = lang(new ConfigStringList("fakeInvCache-Whitelist", ImmutableList.of(), "", ""), ALL);
    }

    @Config public static final ConfigHotkey OPEN_GUI = lang(new ConfigHotkey("openGui", "R,B", "Open Gui Key"), ALL);
    @Config public static final ConfigBooleanHotkeyed DEBUG = lang(new ConfigBooleanHotkeyed("debug", false, "", ""), ALL);

    private static final String FILE_PATH = "./config/" + ClientMod.MOD_ID + ".json";
    private static final File CONFIG_DIR = new File("./config");
    public static boolean lockCreativeScreen = false;

    public static void switchMode(ConfigOptionList option) {
        option.setOptionListValue(option.getOptionListValue().cycle(true));
    }

    public static boolean isInList(Object object, List<?> list) {
        return list.contains(object);
    }

    public static boolean isInList(String object, ConfigStringList list) {
        return isInList(object, list.getStrings());
    }

    @Override
    public void load() {
        File settingFile = new File(FILE_PATH);
        if (settingFile.isFile() && settingFile.exists()) {
//#if MC>=260101
            JsonElement jsonElement = JsonUtils.parseJsonFile(settingFile.toPath());
//#else
            //$$ JsonElement jsonElement = JsonUtils.parseJsonFile(settingFile);
//#endif
            if (jsonElement != null && jsonElement.isJsonObject()) {
                JsonObject obj = jsonElement.getAsJsonObject();
                ConfigUtils.readConfigBase(obj, ClientMod.MOD_ID, ConfigsManager.getConfigs(Tab.ALL));
            }
        }
    }

    @Override
    public void save() {
        if ((CONFIG_DIR.exists() && CONFIG_DIR.isDirectory()) || CONFIG_DIR.mkdirs()) {
            JsonObject configRoot = new JsonObject();
            ConfigUtils.writeConfigBase(configRoot, ClientMod.MOD_ID, ConfigsManager.getConfigs(Tab.ALL));
//#if MC>=260101
            JsonUtils.writeJsonToFile(configRoot, Path.of(FILE_PATH));
//#else
            //$$ JsonUtils.writeJsonToFile(configRoot, new File(FILE_PATH));
//#endif
        }
    }
}
