package com.skynet.adplayer.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.skynet.adplayer.common.Constants;
import com.skynet.adplayer.PlayingActivity;
import com.skynet.adplayer.common.StartUpInfo;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class LongRunningService extends Service {
    private static final String TAG = "LongRunningService";
    private static long alarmTime = 30 * Constants.TIME_1_MINUTE; // 定时10s for debug

    public LongRunningService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.cancel(sender);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "query existed new version at " + new Date());
                StartUpInfo startUpinfo = getStartUpUrl();
                if (startUpinfo == null){
                    onStartUpInfoFail();
                    return;
                }
                onStartUpInfo(startUpinfo);
            }
        }).start();




        return super.onStartCommand(intent, flags, startId);
    }

    private void scheduleNextQuery(long waitTime){
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long trigerAtTime = SystemClock.elapsedRealtime() + waitTime;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigerAtTime, pi);
    }
    private void sendMessageToPlayingActivity(int messageCode, Object value){
        Message msg = Message.obtain();
        msg.what = messageCode;
        msg.obj = value;
        if (PlayingActivity.publicHandler == null){
            return;
        }
        PlayingActivity.publicHandler.sendMessage(msg);
    }
    private void onStartUpInfoFail() {
        scheduleNextQuery(5 * Constants.TIME_1_SECOND);
        sendMessageToPlayingActivity(Constants.MESSAGE_STARTUP_INFO_FAIL, null);
    }

    private void onStartUpInfo(StartUpInfo startUpUrl) {
        scheduleNextQuery(10 * Constants.TIME_1_SECOND);
        sendMessageToPlayingActivity(Constants.MESSAGE_STARTUP_INFO_OK, startUpUrl);
    }

    public static StartUpInfo getStartUpUrl() {

        URL url;
        String urlStr = Constants.START_UP_SERVER_ADDRESS;

        HttpURLConnection urlConnection = null;
        try {
            url = new URL(urlStr);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(1000);

            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);
            StringBuilder sb = new StringBuilder();
            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                sb.append(current);
            }
            String jsonStr = sb.toString();
            Log.i(TAG, "RESPONSE: " + jsonStr);
            JSONObject jObject = new JSONObject(jsonStr);

            String startUpUrl = jObject.getString("startUpUrl");
            String checkVersionUrl = jObject.getString("checkVersionUrl");
            String publicMediaServerPrefix = jObject.getString("publicMediaServerPrefix");

            StartUpInfo result = new StartUpInfo();
            result.setCheckVersionUrl(checkVersionUrl);
            result.setStartUpUrl(startUpUrl);
            result.setPublicMediaServerPrefix(publicMediaServerPrefix);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
