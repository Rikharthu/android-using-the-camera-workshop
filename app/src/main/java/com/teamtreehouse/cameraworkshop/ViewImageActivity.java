package com.teamtreehouse.cameraworkshop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ViewImageActivity extends AppCompatActivity {

    public static final String TAG = ViewImageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);

        Intent intent = getIntent();
        // image uri was attached as a piece of data
        Uri imageUri = intent.getData();
        Log.d(TAG,imageUri.toString());
        // we want Picasso with "this" context
        // to load image file at "imageUri" into our ImageView "imageView"

        // (DONE) FIXME если фотка весит слишком много (большое разрешение), то выкинет exception
        Picasso.with(this)
                .load(imageUri) // image uri
                .error(R.drawable.ic_error_black_24dp) // image shown if error occurs
                .resize(500,500).centerCrop() // resize and center-crop image to reduce image size
                .into(imageView); // ImageView where to load image

        // Просто у Z5 такая заебатая камера, что фотки чуть дохуя весят
    }
}
