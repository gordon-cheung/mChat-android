package com.example.macbook.mchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.File;

public class FullImageViewActivity extends MChatActivity {
    private String TAG = FullImageViewActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String path = getIntent().getExtras().getString(AppNotification.IMAGE_DATA);

        ImageView imageView = (ImageView) findViewById(R.id.fullScreenImageView);
        Uri mUri = Uri.fromFile(new File(path));
        Glide.with(MChatApplication.getAppContext()).load(mUri).into(imageView);
    }

    @Override
    protected void onAppNotificationReceived(Intent intent) {

    }
}
