package com.sguserver.remotebotfetch.client.config;

import com.sguserver.remotebotfetch.client.ClientMod;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
//#if MC>=12103
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
//#endif

public class InitHandler implements IInitializationHandler {
    public static final InitHandler INSTANCE = new InitHandler();

    @Override
    public void registerModHandlers() {
//#if MC>=12103
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(ClientMod.MOD_ID, ClientMod.MOD_ID_FANCY, ConfigUi::new));
//#else
        //$$ // malilib 0.21.0 (1.21.1) has no config screen registry yet, the config screen
        //$$ // stays reachable through the "open gui" hotkey and the json config file.
//#endif
    }
}
