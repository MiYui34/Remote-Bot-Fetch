package com.sguserver.remotebotfetch.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint of the universal jar.
 *
 * <p>The universal jar contains both the server and the client side of the mod, so its metadata can
 * only declare the client dependencies as "suggests" (a dedicated server must not be forced to
 * install MaLiLib or Fabric API). This bootstrap therefore checks for them before touching any
 * class that references those APIs, and simply disables the client features when they are missing.
 */
public final class ClientBootstrap implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("remote-bot-fetch-client");

    @Override
    public void onInitializeClient() {
        FabricLoader loader = FabricLoader.getInstance();
        if (!loader.isModLoaded("malilib")) {
            LOGGER.error("MaLiLib is not installed, the Remote Bot Fetch client features are disabled");
            return;
        }
        if (!loader.isModLoaded("fabric-api")) {
            LOGGER.error("Fabric API is not installed, the Remote Bot Fetch client features are disabled");
            return;
        }
        new ClientMod().onInitializeClient();
    }
}
