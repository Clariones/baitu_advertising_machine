package com.skynet.adplayer.common;

import com.skynet.adplayer.activities.MainActivity;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class ImageAdContent extends BaseAdContent {
    private int playDuration;
    private File ImageFile;

    public File getImageFile() {
        return ImageFile;
    }

    public void setImageFile(File imageFile) {
        ImageFile = imageFile;
    }

    public void setPlayDuration(int playDuration) {
        this.playDuration = playDuration;
    }

    public int getPlayDuration() {
        return playDuration;
    }


    public void startToPlay(MainActivity mainActivity) {
        getMainActivity().showPicture(getImageFile());
        getMainActivity().updateBottomStatues("标题：" + getTitle(), null, true);
        long waitTime = getPlayDuration() * 1000l;
        new Timer().schedule(new TimerTask(){

            @Override
            public void run() {
                whenPlayFinished();
            }
        }, waitTime);
    }

    private void whenPlayFinished() {
        synchronized (this){
            notifyAll();
        }
    }
}