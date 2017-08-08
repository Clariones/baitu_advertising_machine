package com.skynet.adplayer.utils;

import android.os.Build;

import java.lang.reflect.Method;

/**
 * Created by clariones on 6/22/17.
 */
public class SystemPropertyUtils {

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
        String model = Build.MODEL;
        if (model.equalsIgnoreCase("c300")){
            return SystemPropertyUtils.getProperty("ro.boot.serialnoext", "unknown");
        }else {
            return Build.SERIAL;
        }
    }
}
