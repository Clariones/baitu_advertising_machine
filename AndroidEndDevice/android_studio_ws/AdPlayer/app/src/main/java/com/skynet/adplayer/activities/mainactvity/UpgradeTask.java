package com.skynet.adplayer.activities.mainactvity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.skynet.adplayer.activities.MainActivity;
import com.skynet.adplayer.common.ApkReleaseInfo;
import com.skynet.adplayer.utils.DownloadUtils;
import com.skynet.adplayer.utils.HttpUtils;
import com.skynet.adplayer.utils.MiscUtils;

public class UpgradeTask extends BasicTask {
    protected MainActivity mainActivity;
    protected Thread taskThread;

    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        setRunning(false);
    }

    @Override
    public void stopAllAndQuit() {
        if (!isRunning()){
            return;
        }
        setRunning(false);
        if (taskThread != null){
            taskThread.interrupt();
            try {
                taskThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void startToRun() {
        if (isRunning()){
            return;
        }

        if (mainActivity.isOfflineState()){
            showToastMessage("网络故障，无法检查更新。");
            return;
        }

        if (mainActivity.getCheckNewVersionUrl() == null){
            showToastMessage("网络故障，请稍后再试。");
            return;
        }
        taskThread = new Thread(){
            public void run(){
                UpgradeTask.this.run();
            }
        };
        setRunning(true);
        taskThread.start();
    }

    public void run(){
        String url = mainActivity.getCheckNewVersionUrl();
        try {
            String response = HttpUtils.getRequestWithUseAgent(url);
            ApkReleaseInfo resp = MiscUtils.createObjectMapper().readValue(response, ApkReleaseInfo.class);

            if (!resp.isSuccess()){
                showToastMessage(resp.getErrMessage());
                setRunning(false);
                return;
            }
            startToDownloadApk(resp.getDownloadUrl(), resp.getReleaseVersion());
        } catch (Exception e) {
            showToastMessage("网络异常，无法获取版本信息。");
            setRunning(false);
        }

    }

    private void startToDownloadApk(String downloadUrl, final String newVersion) {
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        String fileName = destination + "adplayer_upgrade.apk";
        DownloadUtils.startDownloadWork("Download adplayer apk", "adplayer_upgrade.apk",
                fileName, downloadUrl, mainActivity, new DownloadUtils.DownloadContentHandler() {

                    @Override
                    public void onReceive(Context ctxt, Intent intent, BroadcastReceiver broadcastReceiver, Uri downloadTargetUri) {
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        install.setDataAndType(downloadTargetUri, "application/vnd.android.package-archive");
                        //manager.getMimeTypeForDownloadedFile(downloadId));
                        ctxt.startActivity(install);

                        ctxt.unregisterReceiver(broadcastReceiver);
                        mainActivity.hideBottomStatues();
                        setRunning(false);
                    }

                    @Override
                    public void onProgress(int downloadStatus, int bytesDownloaded, int bytesTotal) {
                        final double dl_progress = (bytesDownloaded * 100.0 / bytesTotal) * 100;
                        mainActivity.updateBottomStatues("正在下载" + newVersion + "...", dl_progress, true);
                    }

                    @Override
                    public void onDownloadStart(long downloadId) {
                        // I don't use the downloadId this time
                    }
                });
    }

    private void showToastMessage(final String message) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainActivity, message, Toast.LENGTH_LONG).show();
            }
        });
    }
} 