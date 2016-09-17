package com.teamtreehouse.cameraworkshop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_TAKE_PHOTO = 0;
    public static final int REQUEST_TAKE_VIDEO = 1;
    public static final int REQUEST_PICK_PHOTO = 2;
    public static final int REQUEST_PICK_VIDEO = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    private Uri mMediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    // Returned to this activity after using startActivityForResult()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 1. check if photo/video was succesfully taken (started activity resulted with RESULT_OK
        if (resultCode == RESULT_OK) {
            // 2. check what we requested from started activity (which request code did we attach)

            if (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO) {
                // handle both request - to take photo or pick a photo from gallery

                // if data is not null, then we know that resulted activity was
                // asked to pick a photo from a gallery rather than take a new one from the camera
                if (data != null) {
                    mMediaUri = data.getData();
                }
                // if resulted activity was camera, then mMediaUri already stores image
                // and data variable is null

                // start ViewImageActivity to view photo
                Intent intent = new Intent(this, ViewImageActivity.class);
                intent.setData(mMediaUri);
                startActivity(intent);
            }
            else if (requestCode == REQUEST_TAKE_VIDEO) {
                // handle the result of recorded video

                // ask videoplayer activities to play our video
                Intent intent = new Intent(Intent.ACTION_VIEW, mMediaUri);
                intent.setDataAndType(mMediaUri, "video/*");
                startActivity(intent);
            }
            else if (requestCode == REQUEST_PICK_VIDEO) {
                if (data != null) {
                    Log.i(TAG, "Video content URI: " + data.getData());
                    Toast.makeText(this, "Video content URI: " + data.getData(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.takePhoto)
    void takePhoto() {
        // We request Camera application (via implicit intent and filters)
        // to take a photo and output it in our mMediaUri by specifying intent parameters
        // we do not care how, but we know that Camera app will handle this.

        // prepare file uri, which points to a temporary file
        mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        if (mMediaUri == null) {
            // media storage unavailable
            Toast.makeText(this,
                    "There was a problem accessing your device's external storage.",
                    Toast.LENGTH_LONG).show();
        }
        else {
            // declare implicit intent to use any available camera app
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // specify the camera app where to store the requested media
            // data will be output to our mMediaUri
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
            startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
            // REQUEST_TAKE_PHOTO will be returned in onActivityResult() when the activity exits.
        }
    }

    @OnClick(R.id.takeVideo)
    void takeVideo() {
        // retrieve uri to temporary file
        mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        if (mMediaUri == null) {
            // temporary file was not created
            Toast.makeText(this,
                    "There was a problem accessing your device's external storage.",
                    Toast.LENGTH_LONG).show();
        }
        else {
            // ask apps that can record video to shoot one for us
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // specify parameters:
            // 1. output to our temporary file specified by mMediaUri
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
            // set max duration
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
            // pass an intent
            startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO);
        }
    }

    @OnClick(R.id.pickPhoto)
    void pickPhoto() {
        // we want another app to get content (file)
        Intent pickPhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        // specify which type of files we want (else song or .doc could also be selected)
        pickPhotoIntent.setType("image/*");
        // We use image/* (the actual "*" after backslash) to tell that we want all formats of images:
        // image/jpeg
        // image/bmp
        // image/gif
        // image/jpg
        // image/png

        startActivityForResult(pickPhotoIntent, REQUEST_PICK_PHOTO);
    }

    @OnClick(R.id.pickVideo)
    void pickVideo() {
        Intent pickVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickVideoIntent.setType("video/*");
        /* Capturing a photo or video gives us a path directly to the file
        Whereas picking from the gallery gives us Content URI */
        startActivityForResult(pickVideoIntent, REQUEST_PICK_VIDEO);
    }

    private Uri getOutputMediaFileUri(int mediaType) {
        // check for external storage
        if (isExternalStorageAvailable()) {
            // get the URI

            // 1. Get the external storage directory
            File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            // 2. Create a unique file name
            String fileName = "";
            String fileType = "";
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            // depending on media type set corresponding extension for the file
            if (mediaType == MEDIA_TYPE_IMAGE){
                fileName = "IMG_"+ timeStamp;
                fileType = ".jpg";
            } else if (mediaType == MEDIA_TYPE_VIDEO) {
                fileName = "VID_"+ timeStamp;
                fileType = ".mp4";
            } else {
                return null;
            }

            // 3. Create the file
            File mediaFile;
            try {
                // construct temporary file with passed data: prefix, suffix, directory
                mediaFile = File.createTempFile(fileName, fileType, mediaStorageDir);
                Log.i(TAG, "File: " + Uri.fromFile(mediaFile));

                // 4. Return the file's URI
                return Uri.fromFile(mediaFile);
            }
            catch (IOException e) {
                Log.e(TAG, "Error creating file: " +
                        mediaStorageDir.getAbsolutePath() + fileName + fileType);
            }
        }

        // something went wrong
        return null;
    }

    /** Check if external storage is avaliable */
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        // sd card mounted?
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        else {
            return false;
        }
    }
}














