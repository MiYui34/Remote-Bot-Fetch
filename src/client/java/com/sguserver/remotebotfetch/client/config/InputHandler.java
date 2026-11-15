package com.sguserver.remotebotfetch.client.config;

import com.sguserver.remotebotfetch.client.ClientMod;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.IKeyboardInputHandler;
import fi.dy.masa.malilib.util.StringUtils;

public class InputHandler implements IKeybindProvider, IKeyboardInputHandler {
    public static final InputHandler INSTANCE = new InputHandler();

    @Override
    public void addKeysToMap(IKeybindManager manager) {
        ConfigsManager.KEY_LIST.forEach(hotkey -> manager.addKeybindToMap(hotkey.getKeybind()));
        ConfigsManager.SWITCH_KEY.forEach(hotkey -> manager.addKeybindToMap(hotkey.getKeybind()));
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(ClientMod.MOD_ID, StringUtils.translate("remote-bot-fetch-client.hotkeys.press"), ConfigsManager.KEY_LIST);
        manager.addHotkeysForCategory(ClientMod.MOD_ID, StringUtils.translate("remote-bot-fetch-client.hotkeys.switch"), ConfigsManager.SWITCH_KEY);
    }
}
