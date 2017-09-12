package com.skynet.adplayer.activities.mainactvity;

import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skynet.adplayer.activities.MainActivity;
import com.skynet.adplayer.common.AdMachinePageContent;
import com.skynet.adplayer.common.AdMachinePlayList;
import com.skynet.adplayer.common.Constants;
import com.skynet.adplayer.utils.FileUtils;
import com.skynet.adplayer.utils.HttpUtils;
import com.skynet.adplayer.utils.MiscUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

public class ContentManager {
    protected MainActivity mainActivity;


    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        File baseFolder = getContentBaseFolder();
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
        }
    }

    public File findNewestPlayListFile() {
        File baseFolder = getContentBaseFolder();

        String[] files = baseFolder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Constants.playListFileNamePattern.matcher(name).matches();
            }
        });
        if (files == null || files.length == 0) {
            return null;
        }
        if (files.length == 1) {
            return new File(baseFolder, files[0]);
        }
        String latestFileName = files[0];
        Matcher m = Constants.playListFileNamePattern.matcher(latestFileName);
        m.matches();
        Date lastestFileDate = null;
        try {
            lastestFileDate = Constants.playListDateFormater.parse(m.group(1));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        for (int i = 0; i < files.length; i++) {
            try {
                m = Constants.playListFileNamePattern.matcher(files[i]);
                m.matches();
                Date curFileDate = Constants.playListDateFormater.parse(m.group(1));

                if (curFileDate.getTime() > lastestFileDate.getTime()) {
                    latestFileName = files[i];
                    lastestFileDate = curFileDate;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Log.i("CONTENT_MANAGER", "Using play list file " + latestFileName);
        return new File(baseFolder, latestFileName);
    }

    public File getContentBaseFolder() {
        return new File(Environment.getExternalStorageDirectory(), "contents");
    }

    public void clearGarbage() {
        File baseFolder = getContentBaseFolder();
        // delete all .tmp files first
        File[] files = baseFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return isTemperoryFile(name);
            }
        });
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                File tmpFile = new File(baseFolder, fileName);
                FileUtils.deleteAll(tmpFile);
            }
        }

        // find all play_list files
        files = baseFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Constants.playListFileNamePattern.matcher(name).matches();
            }
        });
        if (files == null || files.length == 0) {
            // no any play_list file, then no any file should be here
            FileUtils.deleteAll(baseFolder);
            baseFolder.mkdirs();
            return;
        }

        // find half-baked play-list files
        ObjectMapper jsonMapper = MiscUtils.createObjectMapper();
        List<File> validPlayListFiles = new ArrayList<File>();
        Set<String> validAdContentFileName = new HashSet<String>();
        for (File playListFile : files) {
            AdMachinePlayList playList = null;
            try {
                playList = jsonMapper.readValue(playListFile, AdMachinePlayList.class);
            } catch (IOException e) {
                e.printStackTrace();
                // has exception, this is invalid
                continue;
            }
            if (playList == null || playList.getPages() == null) {
                continue;
            }
            boolean allFound = true;
            for (AdMachinePageContent page : playList.getPages()) {
                String cachedFileName = FileUtils.calcCachedAdContentFileName(page);
                File cachedFile = new File(baseFolder, cachedFileName);
                if (!cachedFile.exists() || !cachedFile.isFile()) {
                    allFound = false;
                    break;
                }
            }
            if (!allFound) {
                FileUtils.deleteAll(playListFile);
                continue;
            }
            validPlayListFiles.add(playListFile);
            for (AdMachinePageContent page : playList.getPages()) {
                String cachedFileName = FileUtils.calcCachedAdContentFileName(page);
                validAdContentFileName.add(cachedFileName);
            }
        }

        files = baseFolder.listFiles();
        if (files == null || files.length == 0) {
            return; // all files already removed
        }
        for (File file : files) {
            String fileName = file.getName();
            Matcher m = Constants.playListFileNamePattern.matcher(fileName);
            if (m.matches()) {
                continue; // play list file already verified above
            }
            if (validAdContentFileName.contains(fileName)) {
                continue; // used in play list, so it's valid
            }
            // else, not used anywhere,
            FileUtils.deleteAll(file);
        }
    }

    private boolean isTemperoryFile(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return name.trim().toLowerCase().endsWith(Constants.TEMP_FILE_POSTFIX);
    }

    public File saveToTempPlayListFile(AdMachinePlayList playList) throws Exception {
        ObjectMapper objMapper = MiscUtils.createObjectMapper();
        String fileName = FileUtils.calcCachedPlayListFileName();
        File file = new File(getContentBaseFolder(), fileName + Constants.TEMP_FILE_POSTFIX);
        objMapper.writerWithDefaultPrettyPrinter().writeValue(file, playList);
        return file;
    }

    public void downloadAdContentFile(AdMachinePageContent page) throws Exception {
        if (Constants.AD_CONTENT_TYPE_INTRA_IMAGE.equals(page.getContentType()) || Constants.AD_CONTENT_TYPE_INTRA_IMAGE.equals(page.getContentType())) {
            String fileName = FileUtils.calcCachedAdContentFileName(page);
            String urlPrefix = mainActivity.getMediaServerUrlPrefix();
            downloadImageFile(urlPrefix+page.getImageUri(), fileName);
            return;
        }

        Log.e("CONTENT_MANAGER", "Unsupported AD content type " + page.getContentType());
    }

    private void downloadImageFile(String url, String fileName) throws Exception {
        File baseFolder = getContentBaseFolder();
        File tgtFile = new File(baseFolder, fileName);
        if (tgtFile.exists()){
            Log.i("CONTENT_MANAGER", fileName +" already downloaded. Skip.");
            return;
        }

        File tempFile = new File(baseFolder, fileName+Constants.TEMP_FILE_POSTFIX);
        HttpUtils.saveResponseToFile(url, tempFile);
        FileUtils.renameFileByRemoveTempPostfix(tempFile);
    }

    public File getCachedImageFileByName(String fileName) {
        return new File(getContentBaseFolder(), fileName);
    }
}