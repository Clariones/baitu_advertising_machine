package com.skynet.adplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by clariones on 6/20/17.
 */
public class AdWebView extends WebView{


    public AdWebView(Context context) {
        super(context);
        prepareSettings();
    }

    public AdWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepareSettings();
    }

    public AdWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        prepareSettings();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AdWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        prepareSettings();
    }


    protected String getMachineAgent(){

        StringBuilder sb = new StringBuilder();
        sb.append(Build.MANUFACTURER);
        sb.append("/").append(Build.MODEL);
        sb.append("/").append(Build.SERIAL);
        sb.append("/").append(BuildConfig.VERSION_NAME);

        return sb.toString();

    }

    protected void prepareSettings(){

        this.getSettings().setUserAgentString(getMachineAgent());
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setBlockNetworkImage(false);

    }
    public AdWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        prepareSettings();
    }
}
