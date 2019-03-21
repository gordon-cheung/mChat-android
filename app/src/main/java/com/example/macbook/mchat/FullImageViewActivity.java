package com.example.macbook.mchat;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

public class FullImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_view);

        try {
            String path = "/storage/emulated/0/DCIM/Camera/20190319_172019.jpg";
            String path2 = "/storage/emulated/0/DCIM/Camera/20190318_224056.jpg";
            ImageView imageView = (ImageView) findViewById(R.id.fullScreenImageView);
            Uri mUri = Uri.fromFile(new File(path));
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mUri);
            int byteCount = bitmap.getByteCount();
            imageView.setImageBitmap(bitmap);
            //imageView.setRotation(90);
        } catch(Exception ex) {

        }
    }
}
