package com.skynet.adplayer.activities.mainactvity;

import android.util.Log;

import com.skynet.adplayer.activities.MainActivity;
import com.skynet.adplayer.common.AdMachinePageContent;
import com.skynet.adplayer.common.AdMachinePlayList;
import com.skynet.adplayer.common.BaseAdContent;
import com.skynet.adplayer.common.Constants;
import com.skynet.adplayer.common.ImageAdContent;
import com.skynet.adplayer.utils.DateTimeUtils;
import com.skynet.adplayer.utils.FileUtils;
import com.skynet.adplayer.utils.MiscUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class PlayingTask extends BasicTask{
    private static final String TAG = "PLAYING_TASK";
    protected MainActivity mainActivity;
    protected AdMachinePlayList playList;
    private int currentContentIndex;
    private long nextPlayTimeMs;

    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        setRunning(false);
    }

    public void run(){
       while(isRunning()){
           File playListFile = mainActivity.findNewestPlayListFile();
           if (playListFile == null){
               // this step should be unnecessary. but only for java null-pointer exception
               // the logic should guarantee there is valid play list file, then start the playing task.
               Log.e(TAG, "There is no any valid playing list");
               sleep1Sec();
               continue;
           }

           boolean cacheTaskIsRunning = mainActivity.isCachingTaskRunning();
           if (!cacheTaskIsRunning){
               mainActivity.clearGarbage();
               mainActivity.startCacheTask();
           }

           initPlaying(playListFile);
           if (playList == null || playList.getPages() == null || playList.getPages().isEmpty()){
               Log.e(TAG, "There is no any content in play list");
               // this should be not possiable. Server should check the play list, if there is no any content, should return error.
               // then should this is no cached play list in ad machine.
               sleep1Sec();
               continue;
           }

           int pos = 0;
           List<AdMachinePageContent> pages = playList.getPages();
           while(pos < pages.size()) {
               int validPageIndex = findPageNowInTimeRangeFrom(pages, pos);
               if (validPageIndex < pos){
                   // find all over the whole list, loop round to the earlier index
                   break;
               }
               pos = validPageIndex +1; // next possible content position
               AdMachinePageContent page = pages.get(validPageIndex);
               BaseAdContent adContent = null;
               if (Constants.AD_CONTENT_TYPE_INTRA_IMAGE.equals(page.getContentType())
                       || Constants.AD_CONTENT_TYPE_CMC_IMAGE.equals(page.getContentType())){
                   String fileName = FileUtils.calcCachedAdContentFileName(page);
                   ImageAdContent imageContent = new ImageAdContent();
                   File imageFile = mainActivity.getCachedImageFileByName(fileName);
                   imageContent.setImageFile(imageFile);
                   imageContent.setPlayDuration(page.getPlayDuration());
                   imageContent.setMainActivity(mainActivity);
                   imageContent.setTitle(page.getTitle());

                   adContent = imageContent;
               }else{
                   // should not be possible
                   Log.e(TAG, "Unsupport AD type " + page.getContentType());
                   continue;
               }
               adContent.startToPlay(mainActivity);

               adContent.waitingForPlayingDone();
               if (!isRunning()){
                   break; // if not interrupted by play-finish-notify, but stop-thread. need check this.
               }
           }
       }
    }

    private int findPageNowInTimeRangeFrom(List<AdMachinePageContent> pages, int index) {
        int curTimeSec = DateTimeUtils.getTimeSecond(new Date());
        for(int i=0;i<pages.size();i++){
            int pos = (i + index) % pages.size();
            AdMachinePageContent page = pages.get(pos);
            int startTimeSec = page.getStartTimeSec();
            int endTimeSec = page.getEndTimeSec();

            if (DateTimeUtils.inValidTimeRange(curTimeSec, startTimeSec, endTimeSec)){
                return i;
            }
        }

        return index;
    }

    private void initPlaying(File playListFile) {
        try {
            playList = FileUtils.parsePlayListFromFile(playListFile);
        } catch (IOException e) {
            e.printStackTrace();
            playList = null;
            return;
        }

        currentContentIndex = -1;
        nextPlayTimeMs = 0;

        String plMd5Str = playList.toStringForMD5();
        mainActivity.setPlayingListMd5(MiscUtils.md5Hex(plMd5Str));
    }

    private void sleep1Sec() {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
        }
    }

}