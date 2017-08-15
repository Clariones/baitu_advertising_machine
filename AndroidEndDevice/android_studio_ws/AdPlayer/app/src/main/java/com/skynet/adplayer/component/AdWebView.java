package com.skynet.adplayer.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.skynet.adplayer.BuildConfig;
import com.skynet.adplayer.utils.SystemPropertyUtils;

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


    public static String getMachineAgent(){

        StringBuilder sb = new StringBuilder();
        sb.append(Build.MANUFACTURER);
        sb.append("/").append(SystemPropertyUtils.getModel());
        sb.append("/").append(SystemPropertyUtils.getSerialNo());
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
        //this.getSettings().setAppCacheMaxSize(1024*1024*8);//设置缓冲大小，我设的是8M
        //String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        //webseting.setAppCachePath(appCacheDir);
        this.getSettings().setAllowFileAccess(true);
        this.getSettings().setAppCacheEnabled(true);
        this.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }
    public AdWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        prepareSettings();
    }


}
