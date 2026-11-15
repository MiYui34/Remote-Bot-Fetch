package com.sguserver.remotebotfetch.client.config;

import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.util.MCUtils;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;

import static com.sguserver.remotebotfetch.client.config.Configs.LMS;

public class HotkeysCallback implements IHotkeyCallback {
    public static void init() {
        HotkeysCallback hotkeysCallback = new HotkeysCallback();

        for (ConfigHotkey configHotkey : ConfigsManager.KEY_LIST) {
            configHotkey.getKeybind().setCallback(hotkeysCallback);
        }
    }

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key) {
        if (key == Configs.OPEN_GUI.getKeybind()) {
            MCUtils.setScreen(new ConfigUi());
            return true;
        }
        else if (key == LMS.LMS_TAKE_ITEM.getKeybind()) {
            Configs.lockCreativeScreen = true;
            LocalPlayer player = MCUtils.getLocalPlayer();
            Minecraft mc = MCUtils.getMinecraft();
            MCUtils.setScreen(new CreativeModeInventoryScreen(player, player.connection.enabledFeatures(), mc.options.operatorItemsTab().get()));
            return true;
        }
        else if (key == LMS.CLEAN_PLAYER_INV_CHACHE.getKeybind()) {
            ItemStorageDataManager.removeAll();
            return true;
        }
        return false;
    }
}
