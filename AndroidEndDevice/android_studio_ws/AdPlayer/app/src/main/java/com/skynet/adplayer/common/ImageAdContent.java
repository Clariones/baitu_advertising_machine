package com.skynet.adplayer.common;

import com.skynet.adplayer.activities.MainActivity;

public class ImageAdContent extends BaseAdContent {
    private String contentFileName;
    private int playDuration;

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setPlayDuration(int playDuration) {
        this.playDuration = playDuration;
    }

    public int getPlayDuration() {
        return playDuration;
    }


    public void startToPlay(MainActivity mainActivity) {

    }
}