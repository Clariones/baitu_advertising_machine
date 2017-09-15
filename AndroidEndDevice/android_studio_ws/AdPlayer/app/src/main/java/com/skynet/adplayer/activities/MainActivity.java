package com.skynet.adplayer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.skynet.adplayer.R;
import com.skynet.adplayer.activities.mainactvity.CachingTask;
import com.skynet.adplayer.activities.mainactvity.ConfigurationManager;
import com.skynet.adplayer.activities.mainactvity.ContentManager;
import com.skynet.adplayer.activities.mainactvity.ContentPlayer;
import com.skynet.adplayer.activities.mainactvity.MarqueeShower;
import com.skynet.adplayer.activities.mainactvity.PlayingTask;
import com.skynet.adplayer.activities.mainactvity.PollingTask;
import com.skynet.adplayer.activities.mainactvity.StaticTextShower;
import com.skynet.adplayer.activities.mainactvity.StatusShower;
import com.skynet.adplayer.activities.mainactvity.UpgradeTask;
import com.skynet.adplayer.common.AdMachinePageContent;
import com.skynet.adplayer.common.AdMachinePlayList;
import com.skynet.adplayer.common.Constants;
import com.skynet.adplayer.utils.FileUtils;
import com.skynet.adplayer.utils.MiscUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {



    private int showFullScreenFlag = 0
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            | View.SYSTEM_UI_FLAG_IMMERSIVE;

    private static final int MESSAGE_CODE_CHANGE_OFFLINE_STATE = 8000;
    private static final int MESSAGE_CODE_BOTTOM_STAUTS_MESSAGE = 8001;
    private static final int MESSAGE_CODE_HIDE_BOTTOM_STATUS = 8002;
    private static final int MESSAGE_CODE_ERROR_RESPONSE_MESSAGE = 8003;
    private static final int MESSAGE_CODE_HIDE_CONFIG_LAYOUT = 8004;

    public static MainActivity me;
    private StaticTextShower staticTextShower;
    private ContentManager contentManager;
    private PlayingTask playingTask;
    private CachingTask cachingTask;
    private UpgradeTask upgradeTask;
    private PollingTask pollingTask;
    private long offlineStartTime;
    private boolean offlineState;
    private StatusShower statusShower;
    private Handler internalHandler;
    private MarqueeShower marqueeShower;
    private Map<String, String> startUpInfo;
    private String playingListMd5;
    private ContentPlayer contentPlayer;
    private RelativeLayout mainLayout;
    private ConfigurationManager configurationManager;
    private Button mBtnSettigs;
    private Button mBtnUpgrade;
    private Button mBtnTestNextwork;
    private long powerUpTime;

    public long getOfflineStartTime() {
        return offlineStartTime;
    }

    public void setOfflineStartTime(long offlineStartTime) {
        this.offlineStartTime = offlineStartTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this;
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(showFullScreenFlag);

        setContentView(R.layout.activity_main);
        powerUpTime = System.currentTimeMillis();
        initViewComponents();
        //
        // no any functional code allowed before this.
        //

        markOfflineFlag(true);
        tryGetRootPermission();
        marqueeShower.startScollingTask();

        clearGarbage();
        File playListFile = findNewestPlayListFile();
        if (null == playListFile){
            staticTextShower.onStartWithoutAnyContent();
            pollingTask.startToRun();
            return;
        }


        playingTask.startToRun();
        pollingTask.startToRun();
    }

    private void tryGetRootPermission() {
        new Thread(){
            public void run(){
                MiscUtils.tryGetRoot(me);
            }
        }.start();
    }


    public File findNewestPlayListFile() {
        return contentManager.findNewestPlayListFile();
    }

    public void clearGarbage() {
        contentManager.clearGarbage();
    }

    public void initViewComponents() {
        offlineState = true;
        offlineStartTime = System.currentTimeMillis();
        this.staticTextShower = new StaticTextShower();
        staticTextShower.initMembers(this);
        startUpInfo = new HashMap<String, String>();

        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);

        contentManager = new ContentManager();
        contentManager.initMembers(this);

        contentPlayer = new ContentPlayer();
        contentPlayer.initMembers(this);

        playingTask = new PlayingTask();
        playingTask.initMembers(this);

        cachingTask = new CachingTask();
        cachingTask.initMembers(this);

        pollingTask = new PollingTask();
        pollingTask.initMembers(this);

        statusShower = new StatusShower();
        statusShower.initMembers(this);

        marqueeShower = new MarqueeShower();
        marqueeShower.initMembers(this);

        configurationManager = new ConfigurationManager();
        configurationManager.initMembers(this);

        upgradeTask = new UpgradeTask();
        upgradeTask.initMembers(this);

        mBtnSettigs = (Button) findViewById(R.id.btnSettings);
        mBtnSettigs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CODE_OPEN_SETTING_ACTIVITY);
            }
        });

        mBtnUpgrade = (Button) findViewById(R.id.btnUpgrade);
        mBtnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upgradeTask.startToRun();
            }
        });

        mBtnTestNextwork = (Button) findViewById(R.id.btnTestNetwork);
        mBtnTestNextwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.baidu.com"));
                startActivity(browserIntent);
            }
        });



        internalHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                processInternalMessage(msg);
            }
        };
    }

    private void processInternalMessage(Message msg) {
        switch (msg.what){
            case MESSAGE_CODE_CHANGE_OFFLINE_STATE:
                boolean isOffline = msg.arg1 != 0;
                statusShower.showOfflineFlag(isOffline);
                if (!playingTask.isRunning()){
                    if (isOffline) {
                        staticTextShower.onOfflineWithoutAnyShowableContent();
                    }else{
                        staticTextShower.onConnectedWithoutAnyShowableContent();
                    }
                }
                break;
            case MESSAGE_CODE_BOTTOM_STAUTS_MESSAGE:
                Object[] params = (Object[]) msg.obj;
                staticTextShower.showBottomStatusMessage((String)params[0], (Double)params[1]);
                break;
            case MESSAGE_CODE_HIDE_BOTTOM_STATUS:
                staticTextShower.hideBottomStatusBar();
                break;
            case MESSAGE_CODE_ERROR_RESPONSE_MESSAGE:
                staticTextShower.onRetrievePlayListFailed((AdMachinePlayList)msg.obj);
                break;
            case MESSAGE_CODE_HIDE_CONFIG_LAYOUT:
                configurationManager.hide();
        }
    }

    public synchronized boolean isCachingTaskRunning() {
        return cachingTask.isRunning();
    }

    public synchronized void startCacheTask() {
        cachingTask.startToRun();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollingTask.stopAllAndQuit();
        playingTask.stopAllAndQuit();
        cachingTask.stopAllAndQuit();
        upgradeTask.stopAllAndQuit();
    }

    public void onEachMinute() {
        if (!isOfflineState()){
            return;
        }
        long sts = getOfflineStartTime();
        long cts = System.currentTimeMillis();
        if (cts - sts > MiscUtils.getTimeForRestartAfterOffline()){
            Log.i("ON_EACH_MINUTE", "Offline for " + (cts - sts)/1000 + " seconds. Need restart");
            MiscUtils.restart(me);
        }else{
            Log.i("ON_EACH_MINUTE", "Offline for " + (cts - sts)/1000 + " seconds. If more than " + (MiscUtils.getTimeForRestartAfterOffline()/1000) + " will restart");
        }
    }

    public boolean isOfflineState() {
        return offlineState;
    }

    public void setOfflineState(boolean offlineState) {
        this.offlineState = offlineState;
    }

    public void markOfflineFlag(boolean isOffline) {
        if (isOfflineState() == false && isOffline){
            setOfflineStartTime(System.currentTimeMillis());
        }
        setOfflineState(isOffline);
        Message msg = new Message();
        msg.what = MESSAGE_CODE_CHANGE_OFFLINE_STATE;
        msg.arg1 =  isOffline ? 1 : 0;
        internalHandler.sendMessage(msg);

    }

    public boolean isPlayingTaskRunning() {
        return playingTask.isRunning();
    }

    public void onCachingFinished(int cachingResult) {
        // TODO
    }

    public String getPlaylistRetrieveUrl() {
        return startUpInfo.get(Constants.PARAM_RETRIEVE_PLAYLIST_URL);
    }

    public void onRetrievePlayListFailed(AdMachinePlayList playList) {
        if (!isPlayingTaskRunning()){
            Message msg = new Message();
            msg.what = MESSAGE_CODE_ERROR_RESPONSE_MESSAGE;
            msg.obj = playList;
            internalHandler.sendMessage(msg);
        }
    }

    public String getMediaServerUrlPrefix() {
        return startUpInfo.get(Constants.PARAM_PUBLIC_MEDIA_SERVER_PREFIX);
    }

    public boolean compareWithPlayingList(AdMachinePlayList playList) {
        String str4Md5 = playList.toStringForMD5();
        String md5Hex = MiscUtils.md5Hex(str4Md5);

        return md5Hex.equals(getPlayingListMd5());
    }


    public File savePlayListFile(AdMachinePlayList playList) throws Exception{
        return contentManager.saveToTempPlayListFile(playList);
    }

    public synchronized void markPlayListProcessingDone(File playListFile) {
        FileUtils.renameFileByRemoveTempPostfix(playListFile);
        if (!isPlayingTaskRunning()){
            playingTask.startToRun();
        }
    }

    public void downloadAdContentFile(AdMachinePageContent page) throws Exception {
        contentManager.downloadAdContentFile(page);
    }

    public void updateBottomStatues(String message, Double ratio, boolean forceDisplay) {
        if (forceDisplay || !isPlayingTaskRunning()){
            Message msg = new Message();
            msg.what = MESSAGE_CODE_BOTTOM_STAUTS_MESSAGE;
            msg.obj = new Object[]{message, ratio};
            internalHandler.sendMessage(msg);
        }
    }

    public void hideBottomStatues() {
        Message msg = new Message();
        msg.what = MESSAGE_CODE_HIDE_BOTTOM_STATUS;
        internalHandler.sendMessage(msg);
    }

    public void updateStartUpInfo(Map<String, String> info) {
        startUpInfo.putAll(info);
    }

    public String getPlayingListMd5() {
        return playingListMd5;
    }

    public void setPlayingListMd5(String playingListMd5) {
        this.playingListMd5 = playingListMd5;
    }

    public File getCachedImageFileByName(String fileName) {
        return contentManager.getCachedImageFileByName(fileName);
    }

    public void showPicture(File imageFile) {
        contentPlayer.displayLocalImageFile(imageFile);
    }

    public void deleteOtherPlayListFile(File playListFile) {
        contentManager.deleteOtherPlayListFile(playListFile);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_MOVE){
            //Log.i("======================", "X=" + event.getX()+", Y="+event.getY());
            if (configurationManager.inRectangleRange(event.getX(), event.getY())){
                configurationManager.onMoveInRange();
                return true;
            }
        }else if (action == MotionEvent.ACTION_DOWN){
            if (!configurationManager.inRectangleRange(event.getX(), event.getY())
                    && configurationManager.isShowingup()){
                configurationManager.hide();
                return true;
            }
        }
        return false;
    }

    public void hideConfigLayout() {
        Message msg = new Message();
        msg.what = MESSAGE_CODE_HIDE_CONFIG_LAYOUT;
        internalHandler.sendMessage(msg);
    }

    public String getCurrentAdminPassword() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREF_KEY_ADMIN_PASSWORD, Constants.DEFAULT_ADMIN_PASSWORD);
    }

    public String getPasswordUpdateUrl() {
        return startUpInfo.get(Constants.PARAM_UPDATE_ADMIN_PASSWORD_URL);
    }

    public void setAdminPassword(String newPassword) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Constants.PREF_KEY_ADMIN_PASSWORD, newPassword).commit();
    }

    public String getCheckNewVersionUrl() {
        return startUpInfo.get(Constants.PARAM_CHECK_NEW_APK_URL);
    }

    public String getReportDisplayUrl() {
        return startUpInfo.get(Constants.PARAM_REPORT_DISPLAY_URL);
    }

    public long getPowerUpTime() {
        return powerUpTime;
    }
}
