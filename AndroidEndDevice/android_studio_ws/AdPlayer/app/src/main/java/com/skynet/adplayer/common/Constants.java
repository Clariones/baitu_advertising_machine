package com.skynet.adplayer.common;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

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



    public static final long FORCE_RELOAD_TIME_PERIOD = 12 * TIME_1_HOUR;

    public static final String DEPLOY_DONE_FILE = "deploy.done";

    public static final String PREF_KEY_CONTENT_URL = "pref_key_content_url";
    public static final String PREF_KEY_ADMIN_PASSWORD = "pref_key_admin_password";

    public static final String TEXT_SROLLING = "广告投放服务热线：021-61552739 15221370265 ";
    public static final String TEMP_FILE_POSTFIX = ".tmp";

    public static final String AD_CONTENT_TYPE_INTRA_IMAGE = "intra_image";
    public static final String AD_CONTENT_TYPE_CMC_IMAGE = "cmc_image";
    public static final String AD_CONTENT_FILE_TYPE_IMAGE = "image";

    public static final Pattern playListFileNamePattern = Pattern.compile("^playlist_(\\d{8}_\\d{6})\\.json$");
    public static final SimpleDateFormat playListDateFormater = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static final int CACHE_ACTION_FAILED = -1;
    public static final int CACHE_ACTION_SUCCESS = 0;
    public static final int CACHE_ACTION_NO_CHANGE = 1;

    public static final String PARAM_PUBLIC_MEDIA_SERVER_PREFIX = "publicMediaServerPrefix";
    public static final String PARAM_RETRIEVE_PLAYLIST_URL = "startUpUrl";
    public static final String PARAM_UPDATE_ADMIN_PASSWORD_URL = "passwordUpdateUrl";
    public static final String PARAM_REPORT_DISPLAY_URL = "reportDisplayUrl";
    public static final String PARAM_CHECK_NEW_APK_URL = "checkVersionUrl";

    public static final long CONFIGURE_LAYOUT_AUTO_HIDE_TIME_IN_MS = 10000; // 10 Seconds

    public static final String DEFAULT_ADMIN_PASSWORD = "123456";
    public static final String SUPER_ADMIN_PASSWORD = "@BT>$h1001";




    public static final String BUILD_MODE_TEST = "测试环境";
    public static final String BUILD_MODE_PRODUCT = "product";
    public static final String BUILD_MODE_DEVELOP = "开发环境";
    public static final String BUILD_MODE = BUILD_MODE_PRODUCT;

    public static final long RESTART_AFTER_OFFLINE_IN_MS = 60*60*1000; // product: 30s, product 1hour
    public static final long RESTART_AFTER_OFFLINE_IN_MS_TEST = 30*1000; // test: 30s, product 1hour
    public static final long RESTART_AFTER_OFFLINE_IN_MS_DEV = RESTART_AFTER_OFFLINE_IN_MS_TEST; // test: 30s, product 1hour

    // for product
    public static final String START_UP_SERVER_ADDRESS = "http://www.bettbio.com/bettbio_ad/devices_v2/pc_iframe/data/startup/info.json";
    // for test
    public static final String START_UP_SERVER_ADDRESS_TEST = "http://www.bettbio.com/bettbio_ad/devices_v2/pc_iframe/data/startup/info_v2_testenv.json";
    // for develop
    //    public static final String START_UP_SERVER_ADDRESS = "http://192.168.201.47:8080/test_start_up/data/info_10.0.2.2.json";
    public static final String START_UP_SERVER_ADDRESS_DEV = "http://192.168.201.47:8080/test_start_up/data/info_192.168.201.47.json";

    public static final long IMAGE_FILE_SIZE_LIMIT = 500*1024;
//    public static final long RESTART_AFTER_OFFLINE_IN_MS = 30*1000;
}