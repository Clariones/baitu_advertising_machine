package com.skynet.adplayer.common;

import java.util.Date;

/**
 * Created by clariones on 6/22/17.
 */
public class AdPlayerStatus {
    protected Date lastConnectTime;
    protected Date lastDisconnectTime;
    protected Date lastReloadTime;
    protected boolean connected;
    protected boolean playing;
    protected String checkVersionUrl;
    protected String downloadUrlPrex;

    public String getDownloadUrlPrex() {
        return downloadUrlPrex;
    }

    public void setDownloadUrlPrex(String downloadUrlPrex) {
        this.downloadUrlPrex = downloadUrlPrex;
    }

    public String getCheckVersionUrl() {
        return checkVersionUrl;
    }

    public void setCheckVersionUrl(String checkVersionUrl) {
        this.checkVersionUrl = checkVersionUrl;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public Date getLastConnectTime() {
        return lastConnectTime;
    }

    public void setLastConnectTime(Date lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }

    public Date getLastReloadTime() {
        return lastReloadTime;
    }

    public void setLastReloadTime(Date lastReloadTime) {
        this.lastReloadTime = lastReloadTime;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Date getLastDisconnectTime() {
        return lastDisconnectTime;
    }

    public void setLastDisconnectTime(Date lastDisconnectTime) {
        this.lastDisconnectTime = lastDisconnectTime;
    }

    public void onConnectionFail() {
        if (isConnected()||getLastDisconnectTime() == null){
            setLastDisconnectTime(new Date());
        }
        setConnected(false);
    }

    public void onConnectionSuccess(String startUpUrl) {
        setConnected(true);
        setLastConnectTime(new Date());
    }

    public boolean needRefresh() {
        if (!isPlaying() && !isConnected()){
            // // now is show local static html, and no connection, cannot do anything
            return false;
        }

        if (!isPlaying() && isConnected()){
            // now is show local static html, and connected, then reload page
            setLastReloadTime(new Date());
            setPlaying(true);
            setLastDisconnectTime(null);
            return true;
        }

        if (isPlaying() && !isConnected()){
            // now is playing something, but no connection, so keep current page don't touch
            return false;
        }

        // now is playing, and connected, so check if the time is passed long enough
        Date curTime = new Date();
        if (getLastReloadTime() == null){
            setLastReloadTime(curTime);
            setPlaying(true);
            return true;
        }

        if (getLastDisconnectTime() != null){
            long timePassed = curTime.getTime() - getLastDisconnectTime().getTime();
            if (timePassed > Constants.RECONNECT_RELOAD_TIME_PERIOD){
                setLastReloadTime(curTime);
                setLastDisconnectTime(null);
                setPlaying(true);
                return true;
            }
        }
        long timePassed = curTime.getTime() - getLastReloadTime().getTime();
        if (timePassed > Constants.FORCE_RELOAD_TIME_PERIOD){
            setLastReloadTime(curTime);
            setPlaying(true);
            return true;
        }

        return false;
    }
}
