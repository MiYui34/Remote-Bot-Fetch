package com.sguserver.remotebotfetch.client.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class MCUtils {
    private static final Minecraft MC = Minecraft.getInstance();
    private static int tickCount = 0;

    public static int getTickCount() {
        return tickCount;
    }

    public static void tick() {
        tickCount++;
    }

    public static Minecraft getMinecraft() {
        return MC;
    }

    public static LocalPlayer getLocalPlayer() {
        return MC.player;
    }

    public static ClientLevel getLevel() {
        return MC.level;
    }

    public static void executeCommand(String command) {
        com.sguserver.remotebotfetch.client.ClientMod.debugMessage(() -> "Executing command: " + command);
        if (MC.player != null && command != null) {
            MC.player.connection.sendCommand(command.replaceFirst("/", ""));
        }
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static String getItemID(Item item) {
        var id = BuiltInRegistries.ITEM.getKey(item);
        return id.toString();
    }

    public static Screen getScreen() {
        //#if MC>=260200
        return MC.gui.screen();
        //#else
        //$$ return MC.screen;
        //#endif
    }

    public static void setScreen(Screen screen) {
        //#if MC>=260200
        MC.setScreenAndShow(screen);
        //#else
        //$$ MC.setScreen(screen);
        //#endif
    }

    public static boolean isPlayerOnline(String playerName) {
        ClientLevel level = MCUtils.getLevel();
        if (level == null)
            return false;

        for (AbstractClientPlayer player : level.players()) {
            if (PlayerUtils.getName(player).equalsIgnoreCase(playerName))
                return true;
        }
        return false;
    }

    public static ClientPacketListener getConnection() {
        return MCUtils.getMinecraft().getConnection();
    }

    public static CompletableFuture<Void> executeTask(Runnable toRun) {
        return MC.submit(toRun);
    }
}
