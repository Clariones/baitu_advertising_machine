package com.skynet.adplayer.activities.mainactvity;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skynet.adplayer.activities.MainActivity;
import com.skynet.adplayer.utils.HttpUtils;
import com.skynet.adplayer.utils.MiscUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PollingTask extends BasicTask{
    private static final String TAG = "POLLING_TASK";
    private MainActivity mainActivity;
    private static final long HEAR_BEAT_INTERVAL = 10000L; // 10 Seconds
    private boolean networkConnected;
    private boolean serverConnected;
    private long lastHeartBeatTime;
    private long lastMinuteCallbackTime;
    private static final int TASK_HEART_BEAT = 0;
    private static final int TASK_EACH_MINUTE_CALLBACK = 1;
    private ExecutorService pool;

    public boolean isNetworkConnected() {
        return networkConnected;
    }

    public boolean isServerConnected() {
        return serverConnected;
    }

    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void startToRun() {
        lastHeartBeatTime = 0;
        lastMinuteCallbackTime = System.currentTimeMillis();
        networkConnected = false;
        serverConnected = false;
        pool = Executors.newFixedThreadPool(2);

        super.startToRun();
    }

    public void run(){
        while(isRunning()){
            long curTimeMS = System.currentTimeMillis();
            if (curTimeMS - lastHeartBeatTime >= HEAR_BEAT_INTERVAL){
                lastHeartBeatTime = curTimeMS;
                offerTask(TASK_HEART_BEAT);
            }
            int lastRanMinute = getMinuteFormMs(lastMinuteCallbackTime);
            int curMinute = getMinuteFormMs(curTimeMS);
            if (curMinute != lastRanMinute){
                lastMinuteCallbackTime = curTimeMS;
                offerTask(TASK_EACH_MINUTE_CALLBACK);
            }

            if (!mainActivity.isPlayingTaskRunning() && !mainActivity.isCachingTaskRunning() && !mainActivity.isOfflineState()){
                mainActivity.startCacheTask();
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                // nothing to do when interrupted
            }

            if (!mainActivity.isPlayingTaskRunning() && mainActivity.isOfflineState()){
                mainActivity.reCheckOfflinePlaying();
            }
        }
    }

    private void offerTask(int taskType) {
        switch (taskType){
            case TASK_EACH_MINUTE_CALLBACK:
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.onEachMinute();
                    }
                });
                break;
            case TASK_HEART_BEAT:
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        sendHeartBeatToServer();
                    }
                });
                break;
            default:
                Log.w(TAG, "unsupported task type " + taskType);
        }
    }

    private int getMinuteFormMs(long timeInMs) {
        return (int) ((timeInMs / (1000 * 60))%60);
    }

    private void sendHeartBeatToServer() {
        String url = MiscUtils.getStartUpUrl();
        HttpUtils.RequestResult result = HttpUtils.get(url);
        if (result.isNetworkError() || result.isServerError()){
            mainActivity.markOfflineFlag(true);
            return;
        }else{
            mainActivity.markOfflineFlag(false);
        }

        ObjectMapper objMapper = MiscUtils.createObjectMapper();
        try {
            Map<String, String> info = objMapper.readValue(result.getResponseBody(), Map.class);
            mainActivity.updateStartUpInfo(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}