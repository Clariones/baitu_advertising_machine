package com.skynet.adplayer.activities.mainactvity;

import android.view.View;
import android.widget.TextView;

import com.skynet.adplayer.R;
import com.skynet.adplayer.activities.MainActivity;
import com.skynet.adplayer.utils.SystemPropertyUtils;

public class StaticTextShower {
    protected MainActivity mainActivity;
    protected TextView title;
    protected TextView line1;
    protected TextView line2;
    protected View imageView;
    protected View staticView;

    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        title = (TextView) this.mainActivity.findViewById(R.id.static_text_title);
        line1 = (TextView) this.mainActivity.findViewById(R.id.static_text_line1);
        line2 = (TextView) this.mainActivity.findViewById(R.id.static_text_line2);
        imageView = this.mainActivity.findViewById(R.id.image_display_layout);
        staticView = this.mainActivity.findViewById(R.id.static_text_layout);
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
}