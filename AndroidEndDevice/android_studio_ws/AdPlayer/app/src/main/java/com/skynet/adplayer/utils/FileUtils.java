package com.skynet.adplayer.utils;

import java.io.File;

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
}
