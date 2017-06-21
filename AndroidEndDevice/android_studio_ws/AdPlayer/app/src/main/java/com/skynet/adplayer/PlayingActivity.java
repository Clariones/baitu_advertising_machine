package com.skynet.adplayer;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

public class PlayingActivity extends AppCompatActivity {

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

    public static Handler publicHandler;

    private Handler handler = new Handler() {

        // 处理子线程给我们发送的消息。
        @Override
        public void handleMessage(android.os.Message msg) {
            Object obj = msg.obj;
            if (obj instanceof String){
                mWebView.loadUrl((String) obj);
                return;
            }
            if (obj instanceof ApkReleaseInfo){
                if (!upgrading) {
                    upgrading = true;
                    downloadAndInstallApk((ApkReleaseInfo) obj);
                }
                return;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(showFullScreenFlag);

        setContentView(R.layout.activity_playing);
        publicHandler = handler;


        mBtnUpgrade = (Button) findViewById(R.id.btnUpgrade);
        mBtnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doUpgrade();
            }
        });

        mBtnSettigs = (Button) findViewById(R.id.btnSettings);
        mBtnSettigs.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                doSettings();
            }
        });

        mWebView = (AdWebView) findViewById(R.id.webView);
        final String url = Constants.SERVER_URL_PREFIX + Constants.URL_RETRIEVE_AD_LIST;
        Log.i(Constants.LOG_TAG, "Starting to retrieve from " + url);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(url);

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
                if (apkInfo == null){
                    PlayingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PlayingActivity.this, "无法获取版本信息", Toast.LENGTH_LONG).show();
                            hideTitleBar();
                        }
                    });
                    return;
                }
                if (!apkInfo.isSuccess()){
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
        String fileName = "adplayer_" + apkInfo.getReleaseVersion()+".apk";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists())
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();

        //get url of app on server
        String url = apkInfo.getDownloadUrl();

        //set downloadmanager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Download adplayer apk");
        request.setTitle(fileName);

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);
        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;

                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadId);

                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final double dl_progress = (bytes_downloaded * 100.0 / bytes_total) * 100;

                    Log.d(Constants.LOG_TAG, statusMessage(cursor) + " " + bytes_downloaded + "/" + bytes_total + "=" + dl_progress);
                    cursor.close();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                install.setDataAndType(uri, "application/vnd.android.package-archive");
                //manager.getMimeTypeForDownloadedFile(downloadId));
                startActivity(install);

                unregisterReceiver(this);
                finish();
                //upgrading = false;
            }

        };
        //register receiver for when .apk download is compete
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    private String statusMessage(Cursor c) {
        String msg = "???";

        switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
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
        if (n < 1){
            return;
        }

        MotionEvent.PointerCoords point = new MotionEvent.PointerCoords();
        event.getPointerCoords(0, point);
        Log.i(Constants.LOG_TAG, "Point "+point.x+","+point.y);

        if (point.y > 200){
            hideTitleBar();
        }
    }

    private void hideTitleBar() {
        View barView = findViewById(R.id.title_bar);
        barView.setVisibility(View.GONE);
    }

    private void handleMoveEvent(MotionEvent event) {
        int n = event.getPointerCount();
        if (n < 1){
            return;
        }

        MotionEvent.PointerCoords point = new MotionEvent.PointerCoords();
        event.getPointerCoords(0, point);
        Log.i(Constants.LOG_TAG, "Point "+point.x+","+point.y);

        if (point.y < 100){
            View barView = findViewById(R.id.title_bar);
            barView.setVisibility(View.VISIBLE);
        }
    }
}
