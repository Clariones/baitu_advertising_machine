package com.skynet.adplayer;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.skynet.adplayer.common.AdPlayerInfo;
import com.skynet.adplayer.common.AdPlayerStatus;
import com.skynet.adplayer.common.ApkReleaseInfo;
import com.skynet.adplayer.common.Constants;
import com.skynet.adplayer.common.StartUpInfo;
import com.skynet.adplayer.component.AdWebView;
import com.skynet.adplayer.service.LongRunningService;
import com.skynet.adplayer.utils.DownloadUtils;
import com.skynet.adplayer.utils.FileUtils;
import com.skynet.adplayer.utils.UpgradeUtils;
import com.skynet.adplayer.utils.ZipUtils;

import java.io.File;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayingActivity extends AppCompatActivity {

    public static Handler publicHandler;
    public static PlayingActivity me;
    private Button mBtnSettigs;
    private Button mBtnUpgrade;
    private Button mBtnTestOffline;
    private AdWebView mWebView;
    private boolean upgrading = false;
    private int showFullScreenFlag = 0
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            | View.SYSTEM_UI_FLAG_IMMERSIVE;
    private AdPlayerInfo adPlayerInfo;
    private AdPlayerStatus adPlayerStatus;

    private AdPlayerInfo initPlayInfo() {
        AdPlayerInfo info = new AdPlayerInfo();
        info.setVersion(BuildConfig.VERSION_NAME);
        info.setManufacturer(Build.MANUFACTURER);
        info.setModelName(Build.MODEL);
        info.setSerialNumber(Build.SERIAL);
//        info.setServerUrlPrefix(Constants.SERVER_URL_PREFIX);
        return info;
    }

    private File getOfflinePackageBaseFolder(){
        return new File(Environment.getExternalStorageDirectory(), "offline");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this;
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(showFullScreenFlag);

        setContentView(R.layout.activity_playing);
        publicHandler = new Handler() {
            // 处理子线程给我们发送的消息。
            @Override
            public void handleMessage(Message msg) {
                handleStartupMessage(msg);
            }
        };

        File offlineBase = getOfflinePackageBaseFolder();
        if (!offlineBase.isDirectory()) {
            FileUtils.deleteAll(offlineBase);
        }
        if (!offlineBase.exists()){
            if (offlineBase.mkdirs()){
                Log.i(Constants.LOG_TAG, offlineBase+" was created");
            }else{
                Log.i(Constants.LOG_TAG, offlineBase+" cannot be created. No offline feature.");
            }
        }
        adPlayerInfo = initPlayInfo();
        adPlayerStatus = AdPlayerStatus.createInstance(new AdPlayerStatus.AdPlayerStatusChangeListener(){
            public void onCreate(AdPlayerStatus result) {
                Log.i(Constants.LOG_TAG, "AdPlayerStatus was created");
            }
            public void onStateChange(AdPlayerStatus.ACTION action, AdPlayerStatus status, String reason) {
                Log.i(Constants.LOG_TAG, "AdPlayer status changed: "+status.getState()+", " +reason+", " + action);
                onAdPlayerStatusChanged(action, status);
            }
            public void onNewOfflinePackage(String offlinePackageUrl, AdPlayerStatus status, String reson) {
                Log.i(Constants.LOG_TAG, "onNewOfflinePackage("+offlinePackageUrl+","+reson+")");
                downLoadOnfflinePackage(offlinePackageUrl, status);
            }
        });
        checkExistedOfflinePackage();

        mBtnUpgrade = (Button) findViewById(R.id.btnUpgrade);
        mBtnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doUpgrade();
            }
        });

        mBtnSettigs = (Button) findViewById(R.id.btnSettings);
        mBtnSettigs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSettings();
            }
        });

        mWebView = (AdWebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient());

        mWebView.addJavascriptInterface(adPlayerInfo, "playerInfo");
        mWebView.loadUrl("file:///android_asset/www/loading.html");
        Intent intent = new Intent(this, LongRunningService.class);
        startService(intent);
    }

    public void handleStartupMessage(Message msg) {
        switch (msg.what){
            case Constants.MESSAGE_STARTUP_INFO_FAIL:
                adPlayerStatus.onStartUpInfoFail();
                break;
            case Constants.MESSAGE_STARTUP_INFO_OK:
                adPlayerStatus.onStartUpSuccess((StartUpInfo) msg.obj);
                break;
        }
    }

    private static final Pattern ptnPackagePath = Pattern.compile(".*?[/\\\\](\\w+)_(\\d+)");
    private void checkExistedOfflinePackage() {
        File baseFolder = getOfflinePackageBaseFolder();
        if (!baseFolder.exists()){
            Log.i(Constants.LOG_TAG, "Offline package never existed");
            return;
        }
        File[] files = baseFolder.listFiles();
        if (files == null){
            Log.i(Constants.LOG_TAG, "No any available offline package");
            return;
        }
        long pckVersion = 0;
        String pckName = null;
        for(File file : files){
            if (!file.isDirectory()){
                FileUtils.deleteAll(file);
                continue;
            }
            Matcher m = ptnPackagePath.matcher(file.getAbsolutePath());
            if (!m.matches()){
                FileUtils.deleteAll(file);
                continue;
            }
            long ver = Long.parseLong(m.group(2));
            if (ver > pckVersion){
                pckVersion = ver;
                pckName= m.group(1).toLowerCase()+"_"+pckVersion;
            }
        }
        if (pckName != null){
            Log.i(Constants.LOG_TAG, "Found existed offline package " + pckName);
            adPlayerStatus.setCurOfflinePackageName(pckName);
        }else{
            Log.i(Constants.LOG_TAG, "Cannot found any available offline package");
        }
    }

    private void downLoadOnfflinePackage(final String offlinePackageUrl, AdPlayerStatus status) {
        String packageName = AdPlayerStatus.calcOfflinePackageName(offlinePackageUrl);
        String fileName = packageName+".zip";

        final File targetFile = new File(getOfflinePackageBaseFolder(),fileName);

        DownloadUtils.startDownloadWork("下载离线包",packageName+".zip", targetFile.getAbsolutePath(), offlinePackageUrl, this, new DownloadUtils.DownloadContentHandler(){
            private long downloadId;
            @Override
            public void onReceive(Context ctxt, Intent intent, BroadcastReceiver broadcastReceiver, Uri uri) {
                DownloadManager manager = (DownloadManager) ctxt.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(downloadId);

                Cursor cursor = manager.query(q);
                cursor.moveToFirst();

                int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                unregisterReceiver(broadcastReceiver);
                if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                    Log.i(Constants.LOG_TAG, "download finsihed");
                    // step3. unzip and display offline page
                    onOfflineDownloadSuccess(targetFile.getAbsolutePath(), offlinePackageUrl, true);
                }else{
                    Log.i(Constants.LOG_TAG,"download failed: " + downloadStatus);
                    onOfflineDownloadSuccess(null, offlinePackageUrl, false);
                }

            }

            @Override
            public void onProgress(int downloadStatus, int bytesDownloaded, int bytesTotal) {
                Log.i(Constants.LOG_TAG, statusMessage(downloadStatus)+":"+bytesDownloaded+"/"+bytesTotal);
            }

            @Override
            public void onDownloadStart(long downloadId) {
                Log.i(Constants.LOG_TAG, "Start download from " + offlinePackageUrl);
                this.downloadId = downloadId;
            }
        });
    }
    private void onOfflineDownloadSuccess(String fileName, String packageUrl, boolean success) {
        if (!success){
            adPlayerStatus.onOfflinePackageDownloaded(packageUrl, success);
            return;
        }
        File zipFile = new File(fileName);
        File dataFolder = getOfflinePackageBaseFolder();
        String folderName = zipFile.getName();
        folderName = folderName.substring(0, folderName.lastIndexOf('.')); // remove .zip postfix
        final File targetFolder = new File(dataFolder, folderName);
        Log.i(Constants.LOG_TAG, "Will extract to " + targetFolder.getAbsolutePath());
        try{
            ZipUtils.unzip(zipFile, targetFolder);
        }catch (Exception e){
            return;
        }
        adPlayerStatus.onOfflinePackageDownloaded(packageUrl, success);
    }

    private void onAdPlayerStatusChanged(final AdPlayerStatus.ACTION action, final AdPlayerStatus status) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(action){
                    case RELOAD_OFFLINE:
                        File targetFile = new File(getOfflinePackageBaseFolder(), status.getCurOfflinePackageName()+"/index.html");
                        Uri uri = Uri.parse("file://" + targetFile.getAbsolutePath());
                        mWebView.loadUrl("about:blank");
                        mWebView.loadUrl(uri.toString());
                        deleteOtherOfflinePackage(status.getCurOfflinePackageName());
                        break;
                    case RELOAD_ONLINE:
                        mWebView.loadUrl("about:blank");
                        mWebView.loadUrl(status.getStartUpUrl());

                        break;
                    default:
                        break;
                }
            }
        });

    }

    private void deleteOtherOfflinePackage(String curOfflinePackageName) {
        File baseFolder = getOfflinePackageBaseFolder();
        File[] files = baseFolder.listFiles();
        if (files == null || files.length < 1){
            return;
        }
        for(File file:files){
            if (file.getName().equals(curOfflinePackageName)){
                continue;
            }
            FileUtils.deleteAll(file);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        Intent intent = new Intent(this, LongRunningService.class);
        stopService(intent);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return super.onGenericMotionEvent(event);
    }

    @Override
    protected void onResume() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(showFullScreenFlag);
        super.onResume();
    }

    private void doSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_OPEN_SETTING_ACTIVITY);
    }

    private void doUpgrade() {
        if (adPlayerStatus.getCheckVersionUrl() == null){
            Toast.makeText(PlayingActivity.this, "无法获取版本信息", Toast.LENGTH_LONG).show();
            return;
        }
        //Toast.makeText(PlayingActivity.this.getApplicationContext(), "无法获取版本信息", Toast.LENGTH_LONG).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(Constants.LOG_TAG, "query existed new version at " + new Date());
                final ApkReleaseInfo apkInfo = UpgradeUtils.doApkVersionCheck(adPlayerStatus.getCheckVersionUrl());
                if (apkInfo == null) {
                    PlayingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PlayingActivity.this, "无法获取版本信息", Toast.LENGTH_LONG).show();
                            hideTitleBar();
                        }
                    });
                    return;
                }
                if (!apkInfo.isSuccess()) {
                    PlayingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PlayingActivity.this, apkInfo.getErrMessage(), Toast.LENGTH_LONG).show();
                            hideTitleBar();
                        }
                    });
                    return;
                }
                PlayingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlayingActivity.this, "开始下载新的版本", Toast.LENGTH_LONG).show();
                        hideTitleBar();
                    }
                });
                downloadAndInstallApk(apkInfo);
            }
        }).start();
    }
    private void downloadAndInstallApk(ApkReleaseInfo apkInfo) {
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        String fileName = destination + "adplayer_upgrade.apk";
        DownloadUtils.startDownloadWork("Download adplayer apk", "adplayer_upgrade.apk",
                fileName, apkInfo.getDownloadUrl(), this, new DownloadUtils.DownloadContentHandler() {

                    @Override
                    public void onReceive(Context ctxt, Intent intent, BroadcastReceiver broadcastReceiver, Uri downloadTargetUri) {
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        install.setDataAndType(downloadTargetUri, "application/vnd.android.package-archive");
                        //manager.getMimeTypeForDownloadedFile(downloadId));
                        startActivity(install);

                        unregisterReceiver(broadcastReceiver);
                        finish();
                    }

                    @Override
                    public void onProgress(int downloadStatus, int bytesDownloaded, int bytesTotal) {
                        final double dl_progress = (bytesDownloaded * 100.0 / bytesTotal) * 100;
                        Log.d(Constants.LOG_TAG, statusMessage(downloadStatus) + " " + bytesDownloaded + "/" + bytesTotal + "=" + dl_progress);
                    }

                    @Override
                    public void onDownloadStart(long downloadId) {
                        // I don't use the downloadId this time
                    }
                });
    }
    private static String statusMessage(int code) {
        String msg = "???";

        switch (code) {
            case DownloadManager.STATUS_FAILED:
                msg = "Download failed!";
                break;

            case DownloadManager.STATUS_PAUSED:
                msg = "Download paused!";
                break;

            case DownloadManager.STATUS_PENDING:
                msg = "Download pending!";
                break;

            case DownloadManager.STATUS_RUNNING:
                msg = "Download in progress!";
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                msg = "Download complete!";
                break;

            default:
                msg = "Download is nowhere in sight";
                break;
        }

        return (msg);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        View v = getCurrentFocus();
        Log.i(Constants.LOG_TAG, v.getClass().getCanonicalName());
        if (v instanceof WebView) {
            if (action == MotionEvent.ACTION_MOVE) {
                handleMoveEvent(event);
                return false;
            } else if (action == MotionEvent.ACTION_DOWN) {
                handleDownEvent(event);
                return super.dispatchTouchEvent(event);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void handleDownEvent(MotionEvent event) {
        int n = event.getPointerCount();
        if (n < 1) {
            return;
        }

        MotionEvent.PointerCoords point = new MotionEvent.PointerCoords();
        event.getPointerCoords(0, point);
        Log.i(Constants.LOG_TAG, "Point " + point.x + "," + point.y);

        if (point.y > 200) {
            hideTitleBar();
        }
    }

    private void hideTitleBar() {
        View barView = findViewById(R.id.title_bar);
        barView.setVisibility(View.GONE);
    }

    private void handleMoveEvent(MotionEvent event) {
        int n = event.getPointerCount();
        if (n < 1) {
            return;
        }

        MotionEvent.PointerCoords point = new MotionEvent.PointerCoords();
        event.getPointerCoords(0, point);
        Log.i(Constants.LOG_TAG, "Point " + point.x + "," + point.y);

        if (point.y < 100) {
            View barView = findViewById(R.id.title_bar);
            barView.setVisibility(View.VISIBLE);
        }
    }

}
