package com.skynet.adplayer;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.skynet.adplayer.utils.DownloadUtils;

import java.util.Date;

public class PlayingActivity extends AppCompatActivity {

    public static Handler publicHandler;
    private Button mBtnUpgrade;
    private Button mBtnSettigs;
    private AdWebView mWebView;
    private boolean upgrading = false;
    private int showFullScreenFlag = 0
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            | View.SYSTEM_UI_FLAG_IMMERSIVE;
    private Handler handler = new Handler() {

        // 处理子线程给我们发送的消息。
        @Override
        public void handleMessage(android.os.Message msg) {
            Object obj = msg.obj;
            if (obj instanceof String) {
                mWebView.loadUrl((String) obj);
                return;
            }
            if (obj instanceof ApkReleaseInfo) {
                if (!upgrading) {
                    upgrading = true;
                    downloadAndInstallApk((ApkReleaseInfo) obj);
                }
                return;
            }
        }

        ;
    };

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

    private AdPlayerInfo initPlayInfo() {
        AdPlayerInfo info = new AdPlayerInfo();
        info.setVersion(BuildConfig.VERSION_NAME);
        info.setManufacturer(Build.MANUFACTURER);
        info.setModelName(Build.MODEL);
        info.setSerialNumber(Build.SERIAL);
        info.setServerUrlPrefix(Constants.SERVER_URL_PREFIX);
        return info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(showFullScreenFlag);

        setContentView(R.layout.activity_playing);
        publicHandler = handler;
        AdPlayerInfo info = initPlayInfo();

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
        final String url = Constants.SERVER_URL_PREFIX + Constants.URL_RETRIEVE_AD_LIST;
        Log.i(Constants.LOG_TAG, "Starting to retrieve from " + url);
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(PlayingActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }
        });
        //mWebView.loadUrl(url);
        mWebView.addJavascriptInterface(info, "playerInfo");
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
        //Toast.makeText(PlayingActivity.this.getApplicationContext(), "无法获取版本信息", Toast.LENGTH_LONG).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(Constants.LOG_TAG, "query existed new version at " + new Date());
                final ApkReleaseInfo apkInfo = LongRunningService.doApkVersionCheck();
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
