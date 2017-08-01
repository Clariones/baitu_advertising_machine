package com.skynet.adplayer.common;

/**
 * Created by clariones on 6/20/17.
 */
public class Constants {
    public static final boolean IS_PRODUCT = false;

    public static final int REQUEST_CODE_OPEN_SETTING_ACTIVITY = 1001;

    public static final long TIME_1_SECOND = 1000;
    public static final long TIME_1_MINUTE = 60 * TIME_1_SECOND;
    public static final long TIME_1_HOUR = 60 * TIME_1_MINUTE;

  //  public static final String SERVER_URL_PREFIX = "http://ad.bettbio.com:8380/naf/";
//    public static final String SERVER_URL_PREFIX = "http://192.168.1.101:8080/naf/";
    public static final String URL_RETRIEVE_AD_LIST = "playListManager/retrievePlayList/";
    public static final String URL_CHECK_APK_VERSION = "adMachineApkManager/checkReleaseInfo/";

    public static final String LOG_TAG = "BETTBIO-AD-PLAYER";
    public static final int MESSAGE_START_LOADING = 1002;
    public static final int MESSAGE_NEW_VERSION_APK = 1003;
    public static final int MESSAGE_STARTUP_INFO_FAIL = 1004;
    public static final int MESSAGE_STARTUP_INFO_OK = 1005;

    public static final String STATIC_CONTENT_FOLDER = "static";
    public static final String STATIC_CONTENT_FILE = "index.html";


    public static final String START_UP_SERVER_ADDRESS = "http://www.bettbio.com/bettbio_ad/devices_v1/pc_iframe/data/startup/info.json";

    public static final long FORCE_RELOAD_TIME_PERIOD = 12 * TIME_1_HOUR;

    public static final String DEPLOY_DONE_FILE = "deploy.done";

    public static final String PREF_KEY_CONTENT_URL = "pref_key_content_url";
}