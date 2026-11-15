package com.sguserver.remotebotfetch.client;

import com.sguserver.remotebotfetch.client.config.ConfigUi;
import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.config.ConfigsManager;
import com.sguserver.remotebotfetch.client.config.HotkeysCallback;
import com.sguserver.remotebotfetch.client.config.InitHandler;
import com.sguserver.remotebotfetch.client.config.InputHandler;
import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.util.ChatHelper;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class ClientMod implements ClientModInitializer {
    public static final String MOD_ID = "remote-bot-fetch-client";
    public static final String MOD_ID_FANCY = "Remote Bot Fetch Client";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static String version;

    public static void debugMessage(Supplier<String> obj) {
        if (Configs.DEBUG.getBooleanValue()) {
            String s = obj.get();
            ChatHelper.clientChat(ChatHelper.c(s));
            LOGGER.info(s);
        }
    }

    /**
     * The mod id this client code is shipped under: the split client jar uses
     * {@link #MOD_ID}, the universal jar uses the shared "remote-bot-fetch" id.
     */
    private static String modContainerId() {
        FabricLoader loader = FabricLoader.getInstance();
        return loader.isModLoaded(MOD_ID) ? MOD_ID : "remote-bot-fetch";
    }

    @Override
    public void onInitializeClient() {
        version =
                FabricLoader.getInstance()
                    .getModContainer(modContainerId())
                    .orElseThrow()
                    .getMetadata()
                    .getVersion()
                    .getFriendlyString();
        LOGGER.info("Remote Bot Fetch Client v{} is being loaded...", version);

        // The server mod provides the getItem / getStorageData commands. It is a soft dependency so
        // that the client can still be started (and configured) without it, e.g. in dev runs.
        // In the universal jar the server side is part of this very mod ("remote-bot-fetch").
        if (!FabricLoader.getInstance().isModLoaded("remote-bot-fetch-server")
                && !FabricLoader.getInstance().isModLoaded("remote-bot-fetch")) {
            LOGGER.warn(
                    "Remote Bot Fetch Server is not installed, item fetching will not work until it is added to the server");
        }

        ConfigsManager.init();
        Configs.INSTANCE.load();
        HotkeysCallback.init();
        ConfigManager.getInstance().registerConfigHandler(MOD_ID, Configs.INSTANCE);
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.INSTANCE);
        InputEventHandler.getInputManager().registerKeyboardInputHandler(InputHandler.INSTANCE);
        InitializationHandler.getInstance().registerInitializationHandler(InitHandler.INSTANCE);
        ItemStorageDataManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::onEndClientTick);
    }

    /**
     * Opens the malilib config screen of this mod, used by {@link ConfigUi} as its parent screen.
     */
    public static ConfigUi createConfigScreen() {
        return new ConfigUi();
    }
}
