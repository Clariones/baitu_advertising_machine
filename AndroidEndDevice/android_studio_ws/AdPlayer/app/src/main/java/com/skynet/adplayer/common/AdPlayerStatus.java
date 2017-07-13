package com.skynet.adplayer.common;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by clariones on 6/22/17.
 */
public class AdPlayerStatus {
    public static interface AdPlayerStatusChangeListener {

        void onCreate(AdPlayerStatus result);

        void onStateChange(ACTION playingOnline, AdPlayerStatus adPlayerStatusV2, String reason);

        void onNewOfflinePackage(String offlinePackageUrl, AdPlayerStatus adPlayerStatusV2, String string);
    }

    public static enum STATE {
        PLAYIG_STATIC, PLAYING_ONLINE, PLAYING_OFFLINE
    }

    public static enum ACTION {
        DO_NOTHING, RELOAD_ONLINE, RELOAD_OFFLINE
    }

    protected STATE state;
    protected String lastOnlineUrl;
    protected String lastOfflinePackageName;
    protected Date lastOfflinePlayTime;
    protected Date lastOnlinePlayTime;
    protected boolean connected;
    protected String curOfflinePackageName;
    protected Set<String> downloadingOfflinePackageName;
    protected AdPlayerStatusChangeListener listener;

    protected String startUpUrl;
    protected String offlinePackageUrl;
    protected String checkVersionUrl;
    protected String downloadUrlPrex;

    public String getCurOfflinePackageName() {
        return curOfflinePackageName;
    }

    public void setCurOfflinePackageName(String curOfflinePackageName) {
        this.curOfflinePackageName = curOfflinePackageName;
    }

    public String getCheckVersionUrl() {
        return checkVersionUrl;
    }

    public void setCheckVersionUrl(String checkVersionUrl) {
        this.checkVersionUrl = checkVersionUrl;
    }

    public String getDownloadUrlPrex() {
        return downloadUrlPrex;
    }

    public void setDownloadUrlPrex(String downloadUrlPrex) {
        this.downloadUrlPrex = downloadUrlPrex;
    }

    public static AdPlayerStatus createInstance(AdPlayerStatusChangeListener listener) {
        AdPlayerStatus result = new AdPlayerStatus();
        result.downloadingOfflinePackageName = new HashSet<String>();
        result.connected = false;
        result.state = STATE.PLAYIG_STATIC;
        result.listener = listener;
        listener.onCreate(result);
        return result;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public String getLastOnlineUrl() {
        return lastOnlineUrl;
    }

    public void setLastOnlineUrl(String lastOnlineUrl) {
        this.lastOnlineUrl = lastOnlineUrl;
    }

    public String getLastOfflinePackageName() {
        return lastOfflinePackageName;
    }

    public void setLastOfflinePackageName(String lastOfflinePackageName) {
        this.lastOfflinePackageName = lastOfflinePackageName;
    }

    public Date getLastOfflinePlayTime() {
        return lastOfflinePlayTime;
    }

    public void setLastOfflinePlayTime(Date lastOfflinePlayTime) {
        this.lastOfflinePlayTime = lastOfflinePlayTime;
    }

    public Date getLastOnlinePlayTime() {
        return lastOnlinePlayTime;
    }

    public void setLastOnlinePlayTime(Date lastOnlinePlayTime) {
        this.lastOnlinePlayTime = lastOnlinePlayTime;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public AdPlayerStatusChangeListener getListener() {
        return listener;
    }

    public void setListener(AdPlayerStatusChangeListener listener) {
        this.listener = listener;
    }

    public String getStartUpUrl() {
        return startUpUrl;
    }

    public void setStartUpUrl(String startUpUrl) {
        this.startUpUrl = startUpUrl;
    }

    public String getOfflinePackageUrl() {
        return offlinePackageUrl;
    }

    public void setOfflinePackageUrl(String offlinePackageUrl) {
        this.offlinePackageUrl = offlinePackageUrl;
    }

    public void onStartUpSuccess(StartUpInfo startUpInfo) {
        this.setStartUpUrl(startUpInfo.getStartUpUrl());
        this.setCheckVersionUrl(startUpInfo.getCheckVersionUrl());
        this.setDownloadUrlPrex(startUpInfo.getPublicMediaServerPrefix());
        this.setOfflinePackageUrl(startUpInfo.getOfflinePackageUrl());

        refreshOfflinePackage();
        boolean isConnected = isConnectedOnlinePage();

        switch (this.getState()) {
            case PLAYIG_STATIC:
                if (isConnected) {
                    doReloadOnline("First load online page");
                    return;
                }
                // else, no connection found, check if has offline package
                if (hasOfflinePackage()) {
                    doReloadOffline("First load offline pacakge");
                    return;
                }
                break;

            case PLAYING_ONLINE:
                if (!isConnected && hasOfflinePackage()) {
                    doReloadOffline("Disconnected, load offline package");
                    return;
                }
                if (!isConnected) {
                    return;
                }

                long playingOnlineTime = new Date().getTime() - this.getLastOnlinePlayTime().getTime();
                if (alreadyPlayOnlineForAWhile(playingOnlineTime) || isOnlineUrlChanged()) {
                    doReloadOnline("Reload online page");
                    return;
                }
                // else ACTION.DO_NOTHING
                break;

            case PLAYING_OFFLINE:
                if (isConnected) {
                    doReloadOnline("Re-connected, load online page");
                    return;
                }
                if (!hasOfflinePackage()){
                    return;
                }
                long playingOfflineTime = new Date().getTime() - this.getLastOfflinePlayTime().getTime();
                if (alreadyPlayOfflineForAWhile(playingOfflineTime) || isOfflinePackageChanged()){
                    doReloadOffline("Reload offline page");
                    return;
                }
                break;
            default:
                return;

        }

    }

    public void onStartUpInfoFail(){
        switch (this.getState()) {
            case PLAYIG_STATIC:
                if (hasOfflinePackage()) {
                    doReloadOffline("Connect fail. Fist load offline package");
                    return;
                }
                break;

            case PLAYING_ONLINE:
                if (hasOfflinePackage()) {
                    doReloadOffline("Connect fail. Load offline package");
                    return;
                }
                break;

            case PLAYING_OFFLINE:
                if (!hasOfflinePackage()){
                    return;
                }
                long playingOfflineTime = new Date().getTime() - this.getLastOfflinePlayTime().getTime();
                if (alreadyPlayOfflineForAWhile(playingOfflineTime) || isOfflinePackageChanged()){
                    doReloadOffline("Connect fail. Reload offline package");
                    return;
                }
                break;
            default:
                return;

        }
    }

    public void onOfflinePackageDownloaded(String pacakgeUrl, boolean success){
        if (!success){
            downloadingOfflinePackageName.remove(pacakgeUrl);
            String packageName = calcOfflinePackageName(pacakgeUrl);
            return;
        }
        String packageName = calcOfflinePackageName(pacakgeUrl);
        this.setCurOfflinePackageName(packageName);
        downloadingOfflinePackageName.remove(pacakgeUrl);
    }




    /**
     * offlinePackageUrl should be something like http://abc.com/123abc/123abc/xxx_789.zip.
     * Only ZIP format was supported. Last part must be _\d+
     * @param offlinePackageUrl2
     * @return
     */
    protected static final Pattern ptnOfflinePackage = Pattern.compile(".*?[\\\\/](\\w+)_(\\d+)\\.[zZ][iI][pP]");
    public static String calcOfflinePackageName(String offlinePackageUrl) {
        if (offlinePackageUrl == null){
            return null;
        }
        Matcher m = ptnOfflinePackage.matcher(offlinePackageUrl);
        if (!m.matches()){
            return null;
        }
        return (m.group(1)+"_"+m.group(2)).toLowerCase();
    }

    protected void doReloadOffline(String reason) {
        setState(STATE.PLAYING_OFFLINE);
        setLastOfflinePlayTime(new Date());
        setLastOfflinePackageName(this.getCurOfflinePackageName());
        listener.onStateChange(ACTION.RELOAD_OFFLINE, this, reason);
    }
    protected boolean isOfflinePackageChanged() {
        String offlinePackageName = this.getCurOfflinePackageName();
        if (offlinePackageName == null){
            return false;
        }
        if (this.getLastOfflinePackageName() == null){
            return true;
        }
        return !getLastOfflinePackageName().equals(offlinePackageName);
    }
    protected void doReloadOnline(String reason) {
        setState(STATE.PLAYING_ONLINE);
        setLastOnlinePlayTime(new Date());
        setLastOnlineUrl(this.getStartUpUrl());
        listener.onStateChange(ACTION.RELOAD_ONLINE, this, reason);
    }

    protected boolean isOnlineUrlChanged() {
        if (this.getStartUpUrl() == null){
            return false;
        }
        if (this.getLastOnlineUrl() == null){
            return true;
        }
        return !getStartUpUrl().equals(getLastOnlineUrl());
    }

    protected boolean alreadyPlayOnlineForAWhile(long playingOnlineTime) {
        if (this.getLastOnlinePlayTime() == null){
            return true;
        }
        return playingOnlineTime > Constants.FORCE_RELOAD_TIME_PERIOD;
    }
    protected boolean alreadyPlayOfflineForAWhile(long playingOfflineTime) {
        if (this.getLastOfflinePlayTime() == null){
            return true;
        }
        return playingOfflineTime > Constants.FORCE_RELOAD_TIME_PERIOD;
    }


    protected boolean isConnectedOnlinePage() {
        URL url;
        String urlStr = Constants.START_UP_SERVER_ADDRESS;
        boolean connected = false;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(this.getStartUpUrl());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(1000);

            urlConnection.connect();
            connected = urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK;

        } catch (Exception e) {
            e.printStackTrace();
            connected = false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return connected;
    }

    protected boolean hasOfflinePackage() {
        return getCurOfflinePackageName() != null;
    }

    protected void refreshOfflinePackage() {
        if (!Constants.ENABLE_DOWNLOAD){
            return;
        }
        // check if need download offline package
        if (!isNewOfflinePackage()){
            return;
        }
        String pkgName = calcOfflinePackageName(this.getOfflinePackageUrl());
        synchronized (downloadingOfflinePackageName) {
            if (downloadingOfflinePackageName.contains(pkgName)) {
                return; // if already in download list, ignore
            }
            downloadingOfflinePackageName.add(pkgName);
        }
        listener.onNewOfflinePackage(this.getOfflinePackageUrl(), this, "New offline package found");
    }

    private boolean isNewOfflinePackage() {
        String offlinePackageName = calcOfflinePackageName(this.getOfflinePackageUrl());
        if (offlinePackageName == null){
            return false;
        }
        if (this.getLastOfflinePackageName() == null){
            return true;
        }
        return !getLastOfflinePackageName().equals(offlinePackageName);
    }

}
