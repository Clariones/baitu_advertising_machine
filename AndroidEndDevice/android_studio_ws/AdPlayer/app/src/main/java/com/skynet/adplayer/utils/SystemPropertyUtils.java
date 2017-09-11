package com.skynet.adplayer.utils;

import android.os.Build;
import android.os.Environment;

import com.skynet.adplayer.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Created by clariones on 6/22/17.
 */
public class SystemPropertyUtils {
    protected static String model_str = null;
    protected static String sn_str = null;

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public static String getSerialNo(){
        if (sn_str != null){
            return sn_str;
        }

        synchronized (SystemPropertyUtils.class) {
            tryToLoadSimulateSN();
            if (sn_str != null){
                return sn_str;
            }
            String model = getModel();
            if (model.equalsIgnoreCase("c300")) {
                sn_str = SystemPropertyUtils.getProperty("ro.boot.serialnoext", "unknown");
            } else {
                sn_str = Build.SERIAL;
            }
        }
        return sn_str;
    }

    public static String getModel() {
        if (model_str != null){
            return model_str;
        }
        synchronized (SystemPropertyUtils.class) {
            tryToLoadSimulateSN();
            if (model_str != null) {
                return model_str;
            }
            model_str = Build.MODEL;
        }
        return model_str;
    }

    protected static void tryToLoadSimulateSN(){
        File simSnFile = new File(Environment.getExternalStorageDirectory(), "sim_sn.txt");
        if (!simSnFile.exists()){
            return;
        }
        if (!simSnFile.isFile()){
            return;
        }
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(simSnFile));
            sn_str = props.getProperty("sn");
            model_str = props.getProperty("model");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String getVersion(){
        return BuildConfig.VERSION_NAME;
    }

    public static String getDeviceUserAgentString(){
        StringBuilder sb = new StringBuilder();
        sb.append(Build.MANUFACTURER);
        sb.append("/").append(getModel());
        sb.append("/").append(getSerialNo());
        sb.append("/").append(getVersion());

        return sb.toString();
    }
}
