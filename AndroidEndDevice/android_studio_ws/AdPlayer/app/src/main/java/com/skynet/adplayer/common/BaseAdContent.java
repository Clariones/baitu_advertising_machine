package com.skynet.adplayer.common;

import com.skynet.adplayer.activities.MainActivity;

public abstract class BaseAdContent {
    public void waitingForPlayingDone() {
        try {
            this.wait();
        } catch (InterruptedException e) {

        }
    }

    public abstract void startToPlay(MainActivity mainActivity);
}