package com.sguserver.remotebotfetch.client;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.util.CommandUtils;
import com.sguserver.remotebotfetch.client.util.MCUtils;
import net.minecraft.client.Minecraft;

/**
 * Client tick wiring of the LMS (fake player item retrieval) feature.
 */
public final class ClientTickHandler {
    private static final int AUTO_REFRESH_INTERVAL = 20000;

    private ClientTickHandler() {
    }

    public static void onEndClientTick(Minecraft mc) {
        MCUtils.tick();

        ItemStorageDataManager.scanMatchedPlayersAndInteract(mc);

        final int tickCount = MCUtils.getTickCount();
        if (tickCount % AUTO_REFRESH_INTERVAL == 0
                && Configs.LMS.LMS_FETCH_SUPPORT.getBooleanValue()
                && Configs.LMS.LMS_AUTO_REFRESH.getBooleanValue()
                && CommandUtils.canUseCommand("getStorageData")) {
            ItemStorageDataManager.reflushCache();
        }
    }
}
