package com.skynet.adplayer.utils;

import android.util.Log;


import com.skynet.adplayer.common.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by clariones on 6/22/17.
 */
public class ZipUtils {
    public static void unzip1(File zipFile, File targetFolder) throws Exception {
        try {
            File f = targetFolder;
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = targetFolder.getAbsolutePath() + ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        FileOutputStream fout = new FileOutputStream(path, false);
                        try {
                            for (int c = zin.read(); c != -1; c = zin.read()) {
                                fout.write(c);
                            }
                            zin.closeEntry();
                        } finally {
                            fout.close();
                        }
                    }
                }
            } finally {
                zin.close();
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, "Unzip exception", e);
        }
    }

    public static void unzip(File zipFile, File targetFolder) throws Exception {
        if (zipFile == null || targetFolder == null) {
            throw new Exception("unzip input is null");
        }
        if (targetFolder.exists() && !targetFolder.isDirectory()) {
            throw new Exception(targetFolder.getAbsolutePath() + " is not a folder");
        }
        if (targetFolder.exists()) {
            boolean deleted = FileUtils.deleteAll(targetFolder);
            if (!deleted) {
                Log.w(Constants.LOG_TAG, "Delete " + targetFolder.getName() + " failed");
                throw new Exception("Cannot delete existed " + targetFolder.getAbsolutePath());
            }
        }
        boolean done = targetFolder.mkdirs();
        if (!done) {
            throw new Exception("Cannot create folder " + targetFolder.getAbsolutePath());
        }
        ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
        try {
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                File newFile = new File(targetFolder, ze.getName());
                if (ze.isDirectory()) {
                    File unzipFile = newFile;
                    if (!unzipFile.isDirectory()) {
                        unzipFile.mkdirs();
                    }
                } else {
                    FileOutputStream fout = new FileOutputStream(newFile, false);
                    try {
                        for (int c = zin.read(); c != -1; c = zin.read()) {
                            fout.write(c);
                        }
                        zin.closeEntry();
                    } finally {
                        fout.close();
                    }
                }
            }
        } finally {
            zin.close();
        }
    }
}
