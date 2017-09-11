package com.skynet.adplayer.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skynet.adplayer.common.AdMachinePageContent;
import com.skynet.adplayer.common.AdMachinePlayList;
import com.skynet.adplayer.common.Constants;

import java.io.File;
import java.io.IOException;

/**
 * Created by clariones on 6/22/17.
 */
public class FileUtils {
    public static boolean deleteAll(File file) {
        if (file == null){
            return false;
        }
        boolean done = true;
        if (file.isDirectory()){
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File subFile : files) {
                    done = deleteAll(subFile) && done;
                }
            }
        }
        done = file.delete() && done;
        return done;
    }

    public static String calcCachedAdContentFileName(AdMachinePageContent page) {
        if (Constants.AD_CONTENT_TYPE_INTRA_IMAGE.equals(page.getContentType())){
            int uriHashCode = page.getImageUri().hashCode();
            return String.format("%s_%08X.%s", Constants.AD_CONTENT_FILE_TYPE_IMAGE, uriHashCode, getUriPostfix(page.getImageUri()));
        }
        if (Constants.AD_CONTENT_TYPE_CMC_IMAGE.equals(page.getContentType())){
            int uriHashCode = page.getImageUri().hashCode();
            return String.format("%s_%08X.%s", Constants.AD_CONTENT_FILE_TYPE_IMAGE, uriHashCode, getUriPostfix(page.getImageUri()));
        }

        return null;
    }

    public static AdMachinePlayList parsePlayListFromFile(File playListFile) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return jsonMapper.readValue(playListFile, AdMachinePlayList.class);
    }

    private static String getUriPostfix(String imageUri) {
        int pos = imageUri.lastIndexOf('.');
        if (pos < 0){
            return "unkown";
        }
        return imageUri.substring(pos+1);
    }
}
