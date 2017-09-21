package com.skynet.adplayer.activities.mainactvity;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.skynet.adplayer.R;
import com.skynet.adplayer.activities.MainActivity;

import java.io.File;

public class ContentPlayer {

    private MainActivity mainActivity;
    private ImageView imageView;
    private View imageViewLayout;
    private View staticTextLayout;

    public void initMembers(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.imageView = (ImageView) mainActivity.findViewById(R.id.image_display_view);
        this.imageViewLayout = mainActivity.findViewById(R.id.image_display_layout);
        this.staticTextLayout = mainActivity.findViewById(R.id.static_text_layout);
    }

    public void displayLocalImageFile(final File imageFile) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse("file://" + imageFile.getAbsolutePath());
                String fileName = imageFile.getName().toLowerCase();
                if (fileName.endsWith(".gif")){
                    Glide.with(mainActivity).load(uri).asGif().into(imageView);
                }else {
                    imageView.setImageURI(uri);
                }
                staticTextLayout.setVisibility(View.GONE);
                imageViewLayout.setVisibility(View.VISIBLE);
            }
        });

    }
}