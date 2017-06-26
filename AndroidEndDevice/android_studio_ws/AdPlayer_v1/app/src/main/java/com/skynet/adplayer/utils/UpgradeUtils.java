package com.skynet.adplayer.utils;

import android.util.Log;

import com.skynet.adplayer.common.ApkReleaseInfo;
import com.skynet.adplayer.common.Constants;
import com.skynet.adplayer.component.AdWebView;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by clariones on 6/24/17.
 */
public class UpgradeUtils {
    public static ApkReleaseInfo doApkVersionCheck(String urlStr) {

        URL url;

        HttpURLConnection urlConnection = null;
        try {
            url = new URL(urlStr);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", AdWebView.getMachineAgent());

            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);
            StringBuilder sb = new StringBuilder();
            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                sb.append(current);
            }
            String jsonStr = sb.toString();
            Log.i(Constants.LOG_TAG, "RESPONSE: " + jsonStr);
            JSONObject jObject = new JSONObject(jsonStr);
            ApkReleaseInfo  result = new ApkReleaseInfo();
            result.setDownloadUrl(jObject.getString("downloadUrl"));
            result.setErrMessage(jObject.getString("errMessage"));
            String dateStr = jObject.getString("releaseDate");
            if (dateStr != null && !dateStr.isEmpty() && !dateStr.equalsIgnoreCase("null")){
                result.setReleaseDate(new Date(Long.parseLong(dateStr)));
            }
            result.setReleaseVersion(jObject.getString("releaseVersion"));
            result.setSuccess(jObject.getBoolean("success"));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
