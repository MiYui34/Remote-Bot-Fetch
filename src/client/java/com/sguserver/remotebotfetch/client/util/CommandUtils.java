package com.sguserver.remotebotfetch.client.util;

import net.minecraft.client.Minecraft;

public class CommandUtils {
    public static boolean canUseCommand(String command) {
        var handler = Minecraft.getInstance().getConnection();

        if (handler == null)
            return false;

        var dispatcher = handler.getCommands();
        return dispatcher.getRoot().getChild(command) != null;
    }
}
