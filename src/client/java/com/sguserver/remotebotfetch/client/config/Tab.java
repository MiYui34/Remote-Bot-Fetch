package com.sguserver.remotebotfetch.client.config;

import fi.dy.masa.malilib.util.StringUtils;

public enum Tab {
    ALL("remote-bot-fetch-client.config.cate.all");

    public final String translation;

    Tab(String translation) {
        this.translation = translation;
    }

    public String getTranslate() {
        return StringUtils.translate(translation);
    }
}
