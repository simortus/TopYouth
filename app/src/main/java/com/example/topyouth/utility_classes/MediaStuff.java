package com.example.topyouth.utility_classes;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.example.topyouth.R;

public class MediaStuff {
    private static final String TAG = "MediaStuff";
    public static final int CAMERA_REQUEST = 22;
    public static final int GALLERY_REQUEST = 33;
    private final Activity activity;
    private Uri mUri;

    private static String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public MediaStuff(@NonNull Activity activity) {
        this.activity = activity;
    }

    /**
     * Check permission request if SDK > 23
     */
    public static void checkPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CAMERA = 1;
            int REQUEST_GALLERY = 2;
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_GALLERY | REQUEST_CAMERA);
        }
    }

    public Uri getmUri() {
        return this.mUri;
    }

    /**
     * Open camera
     */
    public void takePicture() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NEW PICTURE");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the camerea");
        this.mUri = this.activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        this.activity.startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    /**
     * Open phone gallery
     */
    public void selectPicture() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        this.activity.startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    /**
     * Open dialog to user to chose Media source
     * Camera or Gallery
     */
    public void dialogChoice() {
        final CharSequence[] options = {"CAMERA", "GALLERY", "CANCEL"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add Image");
        builder.setIcon(R.mipmap.top_youth_logo_icone);
        builder.setItems(options, (dialog, which) -> {
            if (options[which].equals("CAMERA")) {
                takePicture();
            } else if (options[which].equals("GALLERY")) {
                selectPicture();
            } else if (options[which].equals("CANCEL")) {
                dialog.dismiss();
            }

        });
        builder.show();
    }

}
