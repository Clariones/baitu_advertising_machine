package com.skynet.adplayer.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.skynet.adplayer.R;
import com.skynet.adplayer.activities.mainactvity.CachingTask;
import com.skynet.adplayer.activities.mainactvity.ContentManager;
import com.skynet.adplayer.activities.mainactvity.PlayingTask;
import com.skynet.adplayer.activities.mainactvity.PollingTask;
import com.skynet.adplayer.activities.mainactvity.StaticTextShower;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private int showFullScreenFlag = 0
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            | View.SYSTEM_UI_FLAG_IMMERSIVE;
    public static MainActivity me;
    private StaticTextShower staticTextShower;
    private ContentManager contentManager;
    private PlayingTask playingTask;
    private CachingTask cachingTask;
    private PollingTask pollingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this;
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(showFullScreenFlag);

        setContentView(R.layout.activity_main);
        initViewComponents();
        //
        // no any functional code allowed before this.
        //

        clearGarbage();
        File playListFile = findNewestPlayListFile();
        if (null == playListFile){
            staticTextShower.onStartWithoutAnyContent();
            startTimerTask();
            return;
        }

        startPlayTask();
        startTimerTask();
    }

    public void startPlayTask() {
        playingTask.startToRun();
    }

    public void startTimerTask() {
        pollingTask.startToRun();
    }

    public File findNewestPlayListFile() {
        return contentManager.findNewestPlayListFile();
    }

    public void clearGarbage() {
        contentManager.clearGarbage();
    }

    public void initViewComponents() {
        this.staticTextShower = new StaticTextShower();
        staticTextShower.initMembers(this);

        contentManager = new ContentManager();
        contentManager.initMembers(this);

        playingTask = new PlayingTask();
        playingTask.initMembers(this);

        cachingTask = new CachingTask();
        cachingTask.initMembers(this);

        pollingTask = new PollingTask();
        pollingTask.initMembers(this);
    }

    public boolean isCachingTaskRunning() {
        return cachingTask.isRunning();
    }

    public void startCacheTask() {
        cachingTask.startToRun();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollingTask.stopAllAndQuit();
        playingTask.stopAllAndQuit();
        cachingTask.stopAllAndQuit();
    }
}
