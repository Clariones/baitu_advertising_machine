package com.skynet.adplayer.activities.mainactvity;

import android.view.View;

import com.skynet.adplayer.R;
import com.skynet.adplayer.activities.MainActivity;

public class StatusShower {
    private MainActivity mainActivity;
    private View statusLayout;
    private View offlineFlag;

    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        statusLayout = mainActivity.findViewById(R.id.flag_icons_bar);
        offlineFlag = mainActivity.findViewById(R.id.offlineFlag);
    }

    public void showOfflineFlag(boolean isOffline) {
        if (isOffline){
            statusLayout.setVisibility(View.VISIBLE);
//            offlineFlag.setVisibility(View.VISIBLE);
        }else{
            statusLayout.setVisibility(View.GONE);
//            offlineFlag.setVisibility(View.VISIBLE);
        }
    }
}