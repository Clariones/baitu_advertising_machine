package com.skynet.adplayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class LongRunningService extends Service {
    private static final String TAG = "LongRunningService";

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
                ApkReleaseInfo apkInfo = doApkVersionCheck();
                if (apkInfo == null){
                    return;
                }
                if (!apkInfo.isSuccess()){
                    Log.i(TAG, "Cannot upgrade: " + apkInfo.getErrMessage());
                    return;
                }
                doUpgrade(apkInfo);
            }
        }).start();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int alarmTime = 10 * 1000; // 定时10s for debug
        long trigerAtTime = SystemClock.elapsedRealtime() + alarmTime;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigerAtTime, pi);


        return super.onStartCommand(intent, flags, startId);
    }

    private void doUpgrade(ApkReleaseInfo apkInfo) {
        Message msg = Message.obtain();
        msg.what = Constants.MESSAGE_NEW_VERSION_APK;
        msg.obj = apkInfo;
        if (PlayingActivity.publicHandler == null){
            return;
        }
        PlayingActivity.publicHandler.sendMessage(msg);
    }

    public static ApkReleaseInfo doApkVersionCheck() {

        URL url;
        String urlStr = Constants.SERVER_URL_PREFIX + Constants.URL_CHECK_APK_VERSION;

        HttpURLConnection urlConnection = null;
        try {
            url = new URL(urlStr);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", AdWebView.getMachineAgent());

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
            ApkReleaseInfo  result = new ApkReleaseInfo();
            result.setDownloadUrl(jObject.getString("downloadUrl"));
            result.setErrMessage(jObject.getString("errMessage"));
            String dateStr = jObject.getString("releaseDate");
            if (dateStr != null && !dateStr.isEmpty() && !dateStr.equalsIgnoreCase("null")){
                result.setReleaseDate(new Date(Long.parseLong(dateStr)));
            }
            result.setReleaseVersion(jObject.getString("releaseVersion"));
            result.setSuccess(jObject.getBoolean("success"));

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
