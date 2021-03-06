package com.skynet.adplayer.common;

import android.webkit.JavascriptInterface;

/**
 * Created by clariones on 6/22/17.
 */

public class AdPlayerInfo {
    protected String version;
    protected String modelName;
    protected String manufacturer;
    protected String serialNumber;
    protected boolean connected;

    @JavascriptInterface
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @JavascriptInterface
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @JavascriptInterface
    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    @JavascriptInterface
    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    @JavascriptInterface
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
