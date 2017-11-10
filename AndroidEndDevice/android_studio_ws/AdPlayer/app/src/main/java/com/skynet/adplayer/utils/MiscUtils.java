package com.skynet.adplayer.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skynet.adplayer.activities.MainActivity;
import com.skynet.adplayer.common.Constants;

import java.security.MessageDigest;

public class MiscUtils {
    @NonNull
    public static ObjectMapper createObjectMapper() {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return jsonMapper;
    }

    public static String md5Hex(String plaintext) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            byte[] btInput = plaintext.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static void restart(MainActivity me) {
        Log.i("RESTART", "I want to restart whole device. But now I can only exit my progress and let system restart the app.");
        //System.exit(0);
        Process proc = null; //关机
        try {
            proc = Runtime.getRuntime().exec(new String[]{"su","-c","reboot"});
            proc.waitFor();
            Log.i("TRY_TO_REBOOT", "interesting, log success of reboot");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("TRY_TO_REBOOT", "failed to reboot");
        }

    }

    public static void tryGetRoot(MainActivity me) {
        Process proc = null; //关机
        try {
            proc = Runtime.getRuntime().exec(new String[]{"su","-c","pwd"});
            proc.waitFor();
            Log.i("TRY_GET_ROOT", "success to get root permission");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("TRY_GET_ROOT", "failed to get root permission");
        }

    }

    public static long getTimeForRestartAfterOffline() {
        if (Constants.BUILD_MODE == Constants.BUILD_MODE_TEST){
            return Constants.RESTART_AFTER_OFFLINE_IN_MS_TEST;
        }else if (Constants.BUILD_MODE == Constants.BUILD_MODE_DEVELOP){
            return Constants.RESTART_AFTER_OFFLINE_IN_MS_DEV;
        }else{
            return Constants.RESTART_AFTER_OFFLINE_IN_MS;
        }
    }

    public static String getStartUpUrl() {
        if (Constants.BUILD_MODE == Constants.BUILD_MODE_TEST){
            return Constants.START_UP_SERVER_ADDRESS_TEST;
        }else if (Constants.BUILD_MODE == Constants.BUILD_MODE_DEVELOP){
            return Constants.START_UP_SERVER_ADDRESS_DEV;
        }else{
            return Constants.START_UP_SERVER_ADDRESS;
        }
    }


}