package com.sguserver.remotebotfetch.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu integration: lets Mod Menu open the Remote Bot Fetch Client config screen.
 *
 * <p>The class is only loaded when Mod Menu is installed, the "modmenu" entrypoint is not called
 * otherwise, so Mod Menu stays an optional dependency.
 */
public class ModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            ConfigUi ui = new ConfigUi();
            ui.setParent(screen);
            return ui;
        };
    }
}
