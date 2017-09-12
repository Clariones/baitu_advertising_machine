package com.skynet.adplayer.activities.mainactvity;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skynet.adplayer.R;
import com.skynet.adplayer.activities.MainActivity;
import com.skynet.adplayer.common.AdMachinePlayList;
import com.skynet.adplayer.utils.SystemPropertyUtils;

public class StaticTextShower {
    protected MainActivity mainActivity;
    protected TextView title;
    protected TextView line1;
    protected TextView line2;
    protected View imageView;
    protected View staticView;

    protected View bottomStatusBar;
    protected TextView bottomTitle;
    protected ProgressBar bottomRatio;

    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        title = (TextView) this.mainActivity.findViewById(R.id.static_text_title);
        line1 = (TextView) this.mainActivity.findViewById(R.id.static_text_line1);
        line2 = (TextView) this.mainActivity.findViewById(R.id.static_text_line2);
        imageView = this.mainActivity.findViewById(R.id.image_display_layout);
        staticView = this.mainActivity.findViewById(R.id.static_text_layout);

        bottomStatusBar = mainActivity.findViewById(R.id.bottom_info_bar);
        bottomTitle = (TextView) mainActivity.findViewById(R.id.bottom_info_title);
        bottomRatio = (ProgressBar) mainActivity.findViewById(R.id.bottom_info_ratio);
    }

    public void onStartWithoutAnyContent() {
        String titleStr="正在连接服务器";
        String message= "正在连接服务器...";
        String deviceInfo = "广告机:" + SystemPropertyUtils.getDeviceUserAgentString();

        displayText(titleStr, deviceInfo, message);

    }

    private void displayText(String titleStr, String line1Str, String line2Str) {
        title.setText(titleStr);

        line1.setText(line1Str);
        line2.setText(line2Str);
        staticView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
    }

    public void onOfflineWithoutAnyShowableContent() {
        String titleStr="    无法连接服务器";
        String message= "无法连接到服务器。请检查网络设置，或者通过上方的电话联系百图公司。";
        String deviceInfo = "广告机:" + SystemPropertyUtils.getDeviceUserAgentString();

        displayText(titleStr, deviceInfo, message);
    }

    public void showBottomStatusMessage(String text, Double ratio) {
        bottomStatusBar.setVisibility(View.VISIBLE);

        if (text != null){
            bottomTitle.setText(text);
            bottomTitle.setVisibility(View.VISIBLE);
        }else{
            bottomTitle.setVisibility(View.GONE);
        }

        if (ratio != null){
            bottomRatio.setProgress(ratio.intValue());
            bottomRatio.setVisibility(View.VISIBLE);
        }else{
            bottomRatio.setVisibility(View.GONE);
        }
    }

    public void hideBottomStatusBar() {
        bottomStatusBar.setVisibility(View.GONE);
    }

    public void onConnectedWithoutAnyShowableContent() {
        String titleStr="正在请求数据";
        String message= "已经成功连接到服务器，正在向服务器请求数据";
        String deviceInfo = "广告机:" + SystemPropertyUtils.getDeviceUserAgentString();

        displayText(titleStr, deviceInfo, message);
    }

    public void onRetrievePlayListFailed(AdMachinePlayList playListResponse) {
        String titleStr="服务尚未完成：";
        String message= playListResponse.getErrMessage();
        String deviceInfo = "广告机:" + playListResponse.getAdMachine().getId();

        displayText(titleStr, deviceInfo, message);
    }


}