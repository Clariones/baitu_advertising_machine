package com.skynet.adplayer.common;

import com.skynet.adplayer.activities.MainActivity;

public abstract class BaseAdContent {
    private MainActivity mainActivity;
    private String title;

    public void waitingForPlayingDone() {
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {

            }
        }
    }

    public abstract void startToPlay(MainActivity mainActivity);

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}