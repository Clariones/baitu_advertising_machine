package com.skynet.adplayer.common;

/**
 * Created by clariones on 6/24/17.
 */
public class StartUpInfo {
    protected String checkVersionUrl;
    protected String startUpUrl;
    protected String publicMediaServerPrefix;

    public String getPublicMediaServerPrefix() {
        return publicMediaServerPrefix;
    }

    public void setPublicMediaServerPrefix(String publicMediaServerPrefix) {
        this.publicMediaServerPrefix = publicMediaServerPrefix;
    }

    public String getCheckVersionUrl() {
        return checkVersionUrl;
    }

    public void setCheckVersionUrl(String checkVersionUrl) {
        this.checkVersionUrl = checkVersionUrl;
    }

    public String getStartUpUrl() {
        return startUpUrl;
    }

    public void setStartUpUrl(String startUpUrl) {
        this.startUpUrl = startUpUrl;
    }
}
