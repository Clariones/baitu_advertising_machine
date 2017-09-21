package com.skynet.adplayer.activities.mainactvity;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skynet.adplayer.R;
import com.skynet.adplayer.activities.MainActivity;
import com.skynet.adplayer.common.Constants;
import com.skynet.adplayer.common.SimpleAjaxResponse;
import com.skynet.adplayer.utils.HttpUtils;
import com.skynet.adplayer.utils.MiscUtils;

import java.util.Timer;
import java.util.TimerTask;

public class ConfigurationManager {
    private MainActivity mainActivity;
    private View layout;
    private Rect position;
    private boolean initialed;
    private Timer autoHideTimer;
    private boolean needPassword = false;
    private boolean isAskingPassword =false;

    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        initialed = false;
        layout = mainActivity.findViewById(R.id.config_buttons_bar);
        layout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        initAfterUIRenderingDone();

                    }
                });
        ;
        position = new Rect();
        autoHideTimer = null;
        isAskingPassword = false;
    }

    private void initAfterUIRenderingDone() {
        if (initialed) {
            return;
        }

        layout.getGlobalVisibleRect(position);
        if (position.bottom < 10) {
            return;
        }
        Log.i("<<<<CONFIG_MANAGE>>>>", "position=" + position);
        layout.setVisibility(View.GONE);
        initialed = true;
    }


    public boolean inRectangleRange(float x, float y) {
        if (!initialed) {
            return false;
        }
        return y < position.bottom && y > position.top;
    }

    public void onMoveInRange() {
        if (isShowingup()) {
            return; // already show, do nothing now.
        }

        if (autoHideTimer != null) {
            autoHideTimer.cancel();
        }

        if (needPassword) {
            showPassowrdDialog();
        } else {
            displayConfigLayout();
        }
    }

    private void showPassowrdDialog() {
        isAskingPassword = true;
        final EditText editText = new EditText(mainActivity);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(mainActivity);
        inputDialog.setTitle("请输入管理员口令").setView(editText);
        inputDialog.setNegativeButton("取消",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isAskingPassword= false;
            }
        });
        inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verifyPassword(editText.getText().toString());
                    }
                });
        inputDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isAskingPassword= false;
            }
        });
        inputDialog.show();
    }

    private void verifyPassword(String inputPwd) {
        String pwdStr = mainActivity.getCurrentAdminPassword();
        if (pwdStr.equals(inputPwd)) {
            updateAdminPassword();
        } else {
            isAskingPassword= false;
            Toast.makeText(mainActivity, "密码错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAdminPassword() {
        new AsyncTask<MainActivity, Integer, SimpleAjaxResponse>(){

            @Override
            protected SimpleAjaxResponse doInBackground(MainActivity... params) {
                if (mainActivity.isOfflineState()){
                    return null;
                }
                String url = mainActivity.getPasswordUpdateUrl();

                try {
                    String response = HttpUtils.getRequestWithUseAgent(url);
                    ObjectMapper objMapper = MiscUtils.createObjectMapper();
                    SimpleAjaxResponse resp = objMapper.readValue(response, SimpleAjaxResponse.class);
                    return resp;
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(SimpleAjaxResponse resp) {

                if (resp == null){
                    Toast.makeText(mainActivity, "网络故障，未更新管理员密码。", Toast.LENGTH_SHORT).show();
                    displayConfigLayout();
                    return;
                }
                if (!resp.isSuccess()){
                    Toast.makeText(mainActivity, "服务器错误，未更新管理员密码。", Toast.LENGTH_SHORT).show();
                    displayConfigLayout();
                    return;
                }
                String newPassword = (String) resp.getData().get("password");
                if (newPassword == null || newPassword.length() < 6){
                    Toast.makeText(mainActivity, "无效的新密码。请联系管理人员检查数据库是否正常。未更新管理员密码。", Toast.LENGTH_LONG).show();
                    displayConfigLayout();
                    return;
                }

                mainActivity.setAdminPassword(newPassword);
                Toast.makeText(mainActivity, "新密码：" + newPassword, Toast.LENGTH_LONG).show();
                displayConfigLayout();
            }
        }.execute(mainActivity);
    }

    private void displayConfigLayout() {
        layout.setVisibility(View.VISIBLE);
        isAskingPassword = false;
        autoHideTimer = new Timer();
        autoHideTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                autoHideTimer = null;
                mainActivity.hideConfigLayout();
            }
        }, Constants.CONFIGURE_LAYOUT_AUTO_HIDE_TIME_IN_MS);

    }

    public void hide() {
        layout.setVisibility(View.GONE);
    }

    public boolean isShowingup() {
        return layout.getVisibility() == View.VISIBLE || isAskingPassword;
    }
}