package com.skynet.adplayer.activities.mainactvity;

import com.skynet.adplayer.activities.MainActivity;

public class CachingTask {
    protected  MainActivity mainActivity;
    protected boolean isRunning = false;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void stopAllAndQuit() {

    }

    public void startToRun() {

    }
}