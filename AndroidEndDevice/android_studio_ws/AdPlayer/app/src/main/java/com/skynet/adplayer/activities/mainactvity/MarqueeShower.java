package com.skynet.adplayer.activities.mainactvity;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.skynet.adplayer.R;
import com.skynet.adplayer.activities.MainActivity;
import com.skynet.adplayer.common.Constants;

public class MarqueeShower {
    private MainActivity mainActivity;
    private HorizontalScrollView scrollLine;
    private boolean running;
    private int curX;
    private int scrollingEnd;
    private Thread mThread;
    private boolean initialed = false;

    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        scrollLine = (HorizontalScrollView) mainActivity.findViewById(R.id.scrollTextLayout);
        scrollLine.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        initAfterUIRenderingDone();

                    }
                });
    }

    private void initAfterUIRenderingDone() {
        if (initialed){
            return;
        }
        initialed = true;
        synchronized (scrollLine){
            scrollLine.notifyAll();
        }
    }

    public void startScollingTask() {
        new Thread(){
            public void run(){
                runningTask();
            }
        }.start();
    }
    public void runningTask(){
        synchronized (scrollLine){
            try {
                scrollLine.wait();
            } catch (InterruptedException e) {

            }
        }
        scrollLine.scrollTo(0,0);
        running = true;
        curX = 0;
        TextView txtView = (TextView) mainActivity.findViewById(R.id.textScrolling);
        Paint paint = txtView.getPaint();
        float textLength = paint.measureText(Constants.TEXT_SROLLING);
        Log.i("===================", "textLength="+textLength);
        //WindowManager wm = mainActivity.getWindowManager();
        Rect rect = new Rect();
        View mainView = mainActivity.findViewById(R.id.main_layout);
        mainView.getLocalVisibleRect(rect);
        int scrollViewWidth =rect.width();
        Log.i("===================", "scrollViewWidth="+scrollViewWidth);

        String contentStr = Constants.TEXT_SROLLING;
//        float width = 0;
//        while(width < (textLength+scrollViewWidth)){
//            contentStr += Constants.TEXT_SROLLING;
//            width += textLength;
//        }
        Spannable spannable = new SpannableString(contentStr);
        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.argb(255, 255,255,255));
        spannable.setSpan(backgroundColorSpan, 0, contentStr.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtView.setTextColor(Color.rgb(0,0,0));

        txtView.setText(spannable);





        txtView.setMinWidth((int) (scrollViewWidth+scrollViewWidth+textLength));
//        mScrollLine.setMinimumWidth((int) (scrollViewWidth+scrollViewWidth+textLength+10));

        FrameLayout.LayoutParams layParam = (FrameLayout.LayoutParams) txtView.getLayoutParams();
        layParam.setMarginStart(scrollViewWidth);
        layParam.width = (int) (textLength+5);
        txtView.setLayoutParams(layParam);

        scrollingEnd = (int) (scrollViewWidth + textLength);

        mThread = new Thread(){
            public void run() {
                while(running) {
                    try {
                        Thread.sleep(50);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateScrolling();
                            }
                        });
                    } catch (InterruptedException e) {

                    }
                }

            }
        };
        mThread.start();
        scrollLine.setVisibility(View.VISIBLE);
    }

    private void updateScrolling() {
        if (curX < scrollingEnd){
            curX += 2;
        }else{
            curX = 0;
        }
//        Log.i("===================", "X="+mCurX+",in " +mScrollingEnd);
        scrollLine.scrollTo(curX, 0);
    }
}