package com.skynet.adplayer.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.skynet.adplayer.utils.SystemPropertyUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Button mBtnSetSnManually;
    private long powerUpTime;
    private static AtomicBoolean initDone = new AtomicBoolean(false);

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
        // 去掉顶部走马灯
        //marqueeShower.startScollingTask();

        clearGarbage();
        File playListFile = findNewestPlayListFile();
        if (null == playListFile){
            staticTextShower.onStartWithoutAnyContent();
            pollingTask.startToRun();
            initDone.set(true);
            return;
        }


        playingTask.startToRun();
        playingTask.setRunning(true);
        pollingTask.startToRun();
        initDone.set(true);
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

        mBtnSetSnManually = (Button) findViewById(R.id.btnSetSnMenually);
        mBtnSetSnManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetSnManuallyDialog();
            }
        });

        internalHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                processInternalMessage(msg);
            }
        };
    }

    private void showSetSnManuallyDialog() {
        final EditText txtModel = new EditText(me);
        txtModel.setText(SystemPropertyUtils.getModel());
        final EditText txtSN = new EditText(me);
        txtSN.setText(SystemPropertyUtils.getSerialNo());
        //editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(me);
        // build view
        LinearLayout layout = new LinearLayout(me);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView lableWarning = new TextView(me);
        lableWarning.setText("警告：手工设置序列号仅用于硬件不规范的情况，会导致已有数据混乱。请小心操作。");
        lableWarning.setTextColor(Color.RED);
        layout.addView(lableWarning);

        TextView lableModel = new TextView(me);
        lableModel.setText("型号：");
        layout.addView(lableModel);
        layout.addView(txtModel);

        TextView lableSN = new TextView(me);
        lableSN.setText("序列号：");
        layout.addView(lableSN);
        layout.addView(txtSN);

        TextView lablePrompt = new TextView(me);
        lablePrompt.setText("说明：两个内容都清空，表示取消手工设置型号和序列号。只能是字母数字和下划线及中划线。\n设置完成后，需要清除现有内容，然后重启。");
        layout.addView(lablePrompt);


        //
        inputDialog.setTitle("手工指定序列号").setView(layout);
        inputDialog.setNegativeButton("取消",  null);
        inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onMenuallySetSN(txtModel.getText().toString().trim(), txtSN.getText().toString().trim());
            }
        });
        inputDialog.show();
    }

    private static final Pattern ptnValidSN = Pattern.compile("[a-zA-Z0-9\\-_]+");
    private void onMenuallySetSN(String model , String sn) {
        if (model.isEmpty() && sn.isEmpty()){
            SystemPropertyUtils.removeManuallySN();
            return;
        }
        Matcher m = ptnValidSN.matcher(model);
        if (!m.matches()){
            Toast.makeText(me, "型号只能是字母数字和下划线以及中划线", Toast.LENGTH_LONG).show();
            return;
        }
        m = ptnValidSN.matcher(sn);
        if (!m.matches()){
            Toast.makeText(me, "序列号只能是字母数字和下划线以及中划线", Toast.LENGTH_LONG).show();
            return;
        }
        SystemPropertyUtils.saveManuallySN(model, sn);
        Toast.makeText(me, "指定的型号和序列号已经保存", Toast.LENGTH_LONG).show();
    }

    private void processInternalMessage(Message msg) {
        if (!initDone.get()){
            return;
        }
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
        if (Constants.CACHE_ACTION_SUCCESS == cachingResult && !isPlayingTaskRunning()){
            playingTask.startToRun();
        }
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


    public void reCheckOfflinePlaying() {
        File playListFile = findNewestPlayListFile();
        if (null == playListFile){
            return;
        }

        playingTask.startToRun();
    }

    public void clearAllPlayList() {
        contentManager.clearAllPlayList();
        //playingTask.stopAllAndQuit();
        setPlayingListMd5("");
    }
}
