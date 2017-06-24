package com.skynet.adplayer.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;

/**
 * Created by clariones on 6/22/17.
 */
public class DownloadUtils {
    public static interface DownloadContentHandler {

        void onReceive(Context ctxt, Intent intent, BroadcastReceiver broadcastReceiver, Uri uri);

        void onProgress(int downloadStatus, int bytesDownloaded, int bytesTotal);

        void onDownloadStart(long downloadId);
    }

    public static void startDownloadWork(String downloadDscp, String downloadTitle,
                                         String savedFileName, String downloadUrl,
                                         final Activity context, final DownloadContentHandler handler) {
        final Uri uri = Uri.parse("file://" + savedFileName);

        //Delete update file if exists
        File file = new File(savedFileName);
        if (file.exists())
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();

        //get url of app on server
        String url = downloadUrl;

        //set downloadmanager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(downloadDscp);
        request.setTitle(downloadTitle);

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);
        handler.onDownloadStart(downloadId);
        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;

                while (downloading) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadId);

                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED) {
                        downloading = false;
                    }

                    handler.onProgress(downloadStatus, bytes_downloaded, bytes_total);
                    cursor.close();

                }

            }
        }).start();
        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                handler.onReceive(ctxt, intent, this, uri);
            }
        };
        //register receiver for when .apk download is compete
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


}
