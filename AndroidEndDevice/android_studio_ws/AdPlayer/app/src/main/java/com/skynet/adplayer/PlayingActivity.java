package com.skynet.adplayer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.skynet.adplayer.common.Constants;
import com.skynet.adplayer.component.AdWebView;
import com.skynet.adplayer.service.LongRunningService;

import java.util.Date;

public class PlayingActivity extends AppCompatActivity {

    public static Handler publicHandler;
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
                        onConnectionSuccess((String)msg.obj);
                        break;
                }
            }
        };;

        adPlayerInfo = initPlayInfo();
        adPlayerStatus = new AdPlayerStatus();
        adPlayerStatus.setConnected(false);
        adPlayerStatus.setPlaying(false);
        adPlayerInfo.setConnected(false);


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


    private void onConnectionSuccess(String startUpUrl) {
        adPlayerStatus.onConnectionSuccess(startUpUrl);
        adPlayerInfo.setConnected(false);
        if (!adPlayerStatus.isPlaying()){
            Toast.makeText(this, "连接成功＠"+new Date(), Toast.LENGTH_SHORT).show();
        }
        if (adPlayerStatus.needRefresh()){
            // TODO debug:
            // startUpUrl = "http://192.168.1.101:8080/naf/playListManager/retrievePlayList/";

            mWebView.loadUrl(startUpUrl);
        }
    }

    private void onConnectionFail() {
        adPlayerStatus.onConnectionFail();
        adPlayerInfo.setConnected(false);
        if (!adPlayerStatus.isPlaying()){
            Toast.makeText(this, "连接失败＠"+new Date(), Toast.LENGTH_SHORT).show();
        }
    }

}
