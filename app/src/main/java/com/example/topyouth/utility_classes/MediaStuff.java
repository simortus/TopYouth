package com.example.topyouth.utility_classes;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.example.topyouth.R;
import com.example.topyouth.login.LoginActivity;

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
    public void checkPermissions(Activity activity) {
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
        wlp.windowAnimations = R.style.Animation_Design_BottomSheetDialog;
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        alertDialog.getWindow().setAttributes(wlp);
        alertDialog.getWindow().setBackgroundDrawable(activity.getDrawable(R.drawable.round_edge_rectangle_button_recycler_view));

        View layoutView = activity.getLayoutInflater().inflate(R.layout.media_choice_layout, null);
        ImageView cameraButton = layoutView.findViewById(R.id.camera_item);
        ImageView galleryButton = layoutView.findViewById(R.id.gallery_item);
        Button cancelButton = layoutView.findViewById(R.id.cancel_item);
        alertDialog.setView(layoutView);

        cameraButton.setOnClickListener(v -> {
            takePicture();
            alertDialog.dismiss();
        });
        galleryButton.setOnClickListener(v -> {
            selectPicture();
            alertDialog.dismiss();
        });


        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });


        alertDialog.show();
//        final CharSequence[] options = {"Camera", "Gallery", "Cancel"};
//        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setTitle("Add media");
//        builder.setIcon(R.mipmap.top_youth_logo_icone);
//        builder.setItems(options, (dialog, which) -> {
//            if (options[which].equals("CAMERA")) {
//                takePicture();
//            } else if (options[which].equals("GALLERY")) {
//                selectPicture();
//            } else if (options[which].equals("CANCEL")) {
//                dialog.dismiss();
//            }
//
//        });
//        builder.show();
    }

}
