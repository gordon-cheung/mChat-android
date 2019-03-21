package com.example.macbook.mchat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class FullImageViewActivity extends MChatActivity {
    private String TAG = FullImageViewActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_view);
        setSupportActionBar((Toolbar) findViewById(R.id.app_toolbar));
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
