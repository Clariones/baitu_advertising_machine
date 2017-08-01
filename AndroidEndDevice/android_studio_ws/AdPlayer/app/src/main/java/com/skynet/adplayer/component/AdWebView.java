package com.skynet.adplayer.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.skynet.adplayer.BuildConfig;

/**
 * Created by clariones on 6/20/17.
 */
public class AdWebView extends WebView{
    protected String cacheFolder;

    public String getCacheFolder() {
        return cacheFolder;
    }

    public void setCacheFolder(String cacheFolder) {
        this.cacheFolder = cacheFolder;
        this.getSettings().setAppCachePath(cacheFolder);
        this.getSettings().setAppCacheMaxSize(20*1024*1024);
    }

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


    public static String getMachineAgent(){

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
        this.getSettings().setUseWideViewPort(true);

        this.getSettings().setAppCacheEnabled(true);
        this.getSettings().setDomStorageEnabled(true);
        this.getSettings().setAllowFileAccess(true);
        this.getSettings().setAppCacheEnabled(true);
        this.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }
    public AdWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        prepareSettings();
    }
}
