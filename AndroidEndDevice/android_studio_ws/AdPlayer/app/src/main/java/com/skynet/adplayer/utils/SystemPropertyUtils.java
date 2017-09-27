package com.skynet.adplayer.utils;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
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
            } else if (model.toLowerCase().indexOf("rk312x") >= 0){
                sn_str = Build.SERIAL;
            } else {
                sn_str = getMacAddressAsString();
            }
        }
        return sn_str;
    }

    private static String getMacAddressAsString() {
        String mac_s= "";
        try {
            byte[] mac;
            NetworkInterface ne= NetworkInterface.getByInetAddress(InetAddress.getByName(getLocalIpAddress()));
            mac = ne.getHardwareAddress();
            mac_s = byte2hex(mac);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mac_s;
    }

    private static String byte2hex(byte[] mac) {
        StringBuilder sb = new StringBuilder();
        if (mac == null || mac.length == 0){
            return "unknown";
        }
        for(byte bMac : mac){
            sb.append(String.format("%02X", bMac));
        }
        return sb.toString();
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("getLocalIpAddress", ex.toString());
        }

        return null;
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
}
