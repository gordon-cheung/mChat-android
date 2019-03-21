package com.example.macbook.mchat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

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

        try {
            ImageView imageView = (ImageView) findViewById(R.id.fullScreenImageView);
            Uri mUri = Uri.fromFile(new File(path));
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mUri);
            imageView.setImageBitmap(bitmap);

        } catch(IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    protected void onAppNotificationReceived(Intent intent) {

    }
}
