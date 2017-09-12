package com.skynet.adplayer.activities.mainactvity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skynet.adplayer.activities.MainActivity;
import com.skynet.adplayer.common.AdMachinePageContent;
import com.skynet.adplayer.common.AdMachinePlayList;
import com.skynet.adplayer.common.Constants;
import com.skynet.adplayer.utils.HttpUtils;
import com.skynet.adplayer.utils.MiscUtils;

import java.io.File;
import java.util.List;

public class CachingTask extends BasicTask {
    protected  MainActivity mainActivity;
    protected Thread taskThread;

    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        setRunning(false);
    }

    @Override
    public void stopAllAndQuit() {
        if (!isRunning()){
            return;
        }
        setRunning(false);
        if (taskThread != null){
            taskThread.interrupt();
            try {
                taskThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void startToRun() {
        if (isRunning()){
            return;
        }
        taskThread = new Thread(){
            public void run(){
                CachingTask.this.run();
            }
        };
        setRunning(true);
        taskThread.start();
    }

    public void run(){
        int cachingResult = cachePlaylistFromServer();
        mainActivity.onCachingFinished(cachingResult);
        setRunning(false);
    }

    private int cachePlaylistFromServer() {
        try{
            String url = mainActivity.getPlaylistRetrieveUrl();
            if (url == null || url.isEmpty()){
                return Constants.CACHE_ACTION_FAILED;
            }
            String playlistJsonStr = HttpUtils.getRequestWithUseAgent(url);
            ObjectMapper objMapper = MiscUtils.createObjectMapper();
            AdMachinePlayList playList = objMapper.readValue(playlistJsonStr, AdMachinePlayList.class);

            if (!playList.isSuccess()){
                mainActivity.onRetrievePlayListFailed(playList);
                return Constants.CACHE_ACTION_FAILED;
            }

            if (mainActivity.compareWithPlayingList(playList)){
                return Constants.CACHE_ACTION_NO_CHANGE;
            }

            mainActivity.updateBottomStatues("正在请求播放列表",null, false);
            File playListFile = mainActivity.savePlayListFile(playList);

            List<AdMachinePageContent> pages = playList.getPages();
            int totalContents = pages.size();
            int downloadedContents = 0;
            mainActivity.updateBottomStatues("正在下载广告内容", 0d, false);
            for(AdMachinePageContent page : pages) {
                mainActivity.downloadAdContentFile(page);
                downloadedContents ++;
                mainActivity.updateBottomStatues("正在下载广告内容", downloadedContents * 100.0 / totalContents, false);
            }
            mainActivity.markPlayListProcessingDone(playListFile);

            return Constants.CACHE_ACTION_SUCCESS;
        }catch (Exception e){
            e.printStackTrace();
            return Constants.CACHE_ACTION_FAILED;
        }finally{
            mainActivity.hideBottomStatues();
        }
    }
}