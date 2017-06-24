package com.skynet.adplayer;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import com.skynet.adplayer.utils.UpgradeUtils;

import java.util.Date;

public class PlayingActivity extends AppCompatActivity {

    public static Handler publicHandler;
    private Button mBtnSettigs;
    private Button mBtnUpgrade;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(showFullScreenFlag);

        setContentView(R.layout.activity_playing);
        publicHandler = new Handler() {
            // 处理子线程给我们发送的消息。
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case Constants.MESSAGE_STARTUP_INFO_FAIL:
                        onConnectionFail();
                        break;
                    case Constants.MESSAGE_STARTUP_INFO_OK:
                        onConnectionSuccess((StartUpInfo) msg.obj);
                        break;
                }
            }
        };;

        adPlayerInfo = initPlayInfo();
        adPlayerStatus = new AdPlayerStatus();
        adPlayerStatus.setConnected(false);
        adPlayerStatus.setPlaying(false);
        adPlayerInfo.setConnected(false);

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


    private void onConnectionSuccess(StartUpInfo startUpinfo) {
        adPlayerStatus.setCheckVersionUrl(startUpinfo.getCheckVersionUrl());
        adPlayerStatus.setDownloadUrlPrex(startUpinfo.getPublicMediaServerPrefix());

        if (!adPlayerStatus.isPlaying() || !adPlayerStatus.isConnected()){
            Toast.makeText(this, "连接成功＠"+new Date(), Toast.LENGTH_SHORT).show();
        }
        adPlayerStatus.onConnectionSuccess(startUpinfo.getStartUpUrl());
        adPlayerInfo.setConnected(false);

        if (adPlayerStatus.needRefresh()){
            // TODO debug:
            //startUpUrl = "http://192.168.1.101:8080/naf/playListManager/retrievePlayList/";

            mWebView.loadUrl(startUpinfo.getStartUpUrl());
        }
    }

    private void onConnectionFail() {
        adPlayerStatus.setCheckVersionUrl(null);

        if (!adPlayerStatus.isPlaying() || adPlayerStatus.isConnected()){
            Toast.makeText(this, "连接失败＠"+new Date(), Toast.LENGTH_SHORT).show();
        }
        adPlayerStatus.onConnectionFail();
        adPlayerInfo.setConnected(false);

    }

}
