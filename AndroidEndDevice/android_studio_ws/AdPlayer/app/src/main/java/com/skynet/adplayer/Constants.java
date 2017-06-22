package com.skynet.adplayer;

/**
 * Created by clariones on 6/20/17.
 */
public class Constants {
    public static final boolean IS_PRODUCT = false;

    public static final int REQUEST_CODE_OPEN_SETTING_ACTIVITY = 1001;

    public static final long TIME_1_SECOND = 1000;
    public static final long TIME_1_MINUTE = 60 * TIME_1_SECOND;
    public static final long TIME_1_HOUR = 60 * TIME_1_MINUTE;

    public static final String SERVER_URL_PREFIX = "http://ad.bettbio.com:8380/naf/";
    public static final String URL_RETRIEVE_AD_LIST = "playListManager/retrievePlayList/";
    public static final String URL_CHECK_APK_VERSION = "adMachineApkManager/checkReleaseInfo/";

    public static final String LOG_TAG = "BETTBIO-AD-PLAYER";
    public static final int MESSAGE_START_LOADING = 1002;
    public static final int MESSAGE_NEW_VERSION_APK = 1003;

}