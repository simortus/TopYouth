package com.example.topyouth.add_post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.topyouth.R;
import com.example.topyouth.home.MainActivity;
import com.example.topyouth.molde.PostModel;
import com.example.topyouth.molde.TopUser;
import com.example.topyouth.utility_classes.DBSingelton;
import com.example.topyouth.utility_classes.Traveler;
import com.example.topyouth.view_utils.BottomNavigationHandler;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class AddPostActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddPostActivity";
    private static final int CAMERA_REQUEST = 22;
    private static final int GALLERY_REQUEST = 33;

    //view
    private BottomNavigationView bottomNavigationView;
    private RelativeLayout notApprovedLayout, userLayout;
    private EditText postDetails;
    private ImageView postImage;
    private ImageButton uploadButton;


    //navigation handlers
    private BottomNavigationHandler bottomNavigationHandler;
    private Context context;

    //firebase
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseFirestore mFirestore;
    private DBSingelton dbSingelton;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    //database refs
    private DatabaseReference postsRef;

    //vars
    private Uri mUri;
    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private TopUser currentTopUser;
    private final Traveler traveler = new Traveler();

    private TopUser getCurrentTopUser() {
        return currentTopUser;
    }

    private void setCurrentTopUser(TopUser currentTopUser) {
        this.currentTopUser = currentTopUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        context = this;
        widgets();
        connectFirebase();

        bottomNavigationHandler = new BottomNavigationHandler(context, bottomNavigationView);
        bottomNavigationHandler.navigation();
        bottomNavigationHandler.isAdminApproved(mUser, notApprovedLayout, userLayout);
        checkPermissions();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        boolean besoins = resultCode == RESULT_OK && requestCode == CAMERA_REQUEST && mUri != null;

        try {

            if (besoins) {
                Glide.with(this).load(mUri).centerCrop().into(postImage);
            } else if (resultCode == RESULT_OK) {
                mUri = data.getData();
                Glide.with(this).load(mUri).centerCrop().into(postImage);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.uploadButton:
                Log.d(TAG, "onClick: uploadButton clicked");
                checkPostForValidity();

                break;

            case R.id.postImageView:
                Log.d(TAG, "onClick: postImageView clicked");
                dialogChoice();
                break;

        }
    }

    private void alertUser(@NonNull final Uri uri, @NonNull final String postDet) {
        final CharSequence[] options = {"Yes", "No"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Post has no details, Upload anyway?");
        builder.setPositiveButton(options[0], (dialogInterface, i) -> {
            final String postDetails = "No details";
            uploadNow(uri, postDetails);

        }).setNegativeButton(options[1], (dialogInterface, i) ->
                dialogInterface.dismiss()
        );

        builder.show();
    }

    private void uploadNow(@NonNull final Uri uri, @NonNull final String postDet) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading, please wait...");
        progressDialog.setIcon(R.mipmap.top_youth_logo_icone);
        progressDialog.setCanceledOnTouchOutside(false);

        final String postOwnerID = mUser.getUid();

        StorageReference ref = storage.getReference("posts");
        String postID = UUID.randomUUID().toString();
        progressDialog.show();
        ref.child(postOwnerID).child(postID).putFile(uri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        String url = task1.toString();
                        createPost(postID, postDet, url);
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "sendPost: addOnFailureListener exception: " + e.getLocalizedMessage());
                });

            }

        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Log.d(TAG, "sendPost: addOnFailureListener exception: " + e.getLocalizedMessage());

        }).addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
            progressDialog.setMessage("uploading " + (int) progress + "%");
        });
    }
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if (task.isSuccessful()) {
//                    //todo continue here
//                    ref.getDownloadUrl().addOnCompleteListener(task1 -> {
//                        String url = task1.toString();
//                        createPost(postID, postDet, url);
//                        progressDialog.dismiss();
//
//                    }).addOnFailureListener(e -> {
//                        progressDialog.dismiss();
//                        Log.d(TAG, "sendPost: addOnFailureListener exception: " + e.getLocalizedMessage());
//                    }).addOnFailureListener(e -> {
//                        progressDialog.dismiss();
//                        Log.d(TAG, "sendPost: addOnFailureListener exception: " + e.getLocalizedMessage());
//
//                    }).addOnProgressListener(snapshot -> {
//                        //todo continue here
//                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
//                        progressDialog.setMessage("uploading " + (int) progress + "%");
//
//                    });


    private void checkPostForValidity() {
        final String postDet = postDetails.getText().toString();
        final Uri uri = mUri;
        if (uri != null) {
            if (postDet.isEmpty()) {
                alertUser(uri, postDet);
            } else {
                uploadNow(uri, postDet);
            }

        } else {
            Toast.makeText(context, "Please select a media", Toast.LENGTH_SHORT).show();
        }
    }

    private TopUser topUser() {
        final String userId = mUser.getUid();
        DatabaseReference usersRef = database.getReference("users");
        Query query = usersRef.child(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    TopUser user = dataSnapshot.getValue(TopUser.class);
                    Log.d(TAG, "onDataChange: user details: " + user.toString());
                    setCurrentTopUser(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: databaseError: " + databaseError.getMessage());
            }
        });
        return getCurrentTopUser();
    }

    private void createPost(@NonNull final String postID,
                            @NonNull final String postDet, @NonNull final String url) {
        final TopUser user = topUser();
        final String userID = user.getUserId();

        // todo contiunue here
        PostModel post = new PostModel(postID, url, postDet, user.getUserId());
        final DatabaseReference postRef = database.getReference("posts");
        Query query = postRef.child(user.getUserId()).child(postID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: snapshot do not exist: ");
                    postRef.child(userID).child(postID).setValue(post).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: post added to database");
                            Toast.makeText(context,"Uploaded successfully",Toast.LENGTH_SHORT).show();
                            postImage.setImageDrawable(getResources().getDrawable(R.color.yellowish));
                            postImage.refreshDrawableState();
                            postDetails.clearComposingText();
                            traveler.gotoWithFlags(context, MainActivity.class);
                        }
                    }).addOnFailureListener(e ->
                            Log.d(TAG, "onFailure: Exception: " + e.getLocalizedMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onFailure: Exception: " + databaseError.getMessage());
            }
        });

    }

    private void widgets() {
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        notApprovedLayout = findViewById(R.id.not_approved_layout);
        userLayout = findViewById(R.id.userLayout);

        postDetails = findViewById(R.id.postDetailsEditText);
        postImage = findViewById(R.id.postImageView);
        uploadButton = findViewById(R.id.uploadButton);

        //click listeners
        postImage.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
    }


    private void connectFirebase() {
        authSingleton = FirebaseAuthSingleton.getInst(context);
        mAuth = authSingleton.mAuth();
        mUser = authSingleton.getCurrentUser();
        dbSingelton = DBSingelton.getInstance();
        database = dbSingelton.getDbInstance();
        mFirestore = dbSingelton.getmFirestore();
        storage = dbSingelton.getStorage();
        postsRef = dbSingelton.getPostsRef();
        topUser();
        mAuthStateListener = firebaseAuth -> {
            mAuth.addAuthStateListener(mAuthStateListener);
        };
    }


    /**
     * Open dialog to user to chose Media source
     * Camera or Gallery
     */
    private void dialogChoice() {
        final CharSequence[] options = {"CAMERA", "GALLERY", "CANCEL"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

    /**
     * Check permission request if SDK > 23
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CAMERA = 1;
            int REQUEST_GALLERY = 2;
            ActivityCompat.requestPermissions(this, permissions, REQUEST_GALLERY | REQUEST_CAMERA);
        }
    }

    /**
     * Open camera
     */
    private void takePicture() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NEW PICTURE");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the camerea");
        mUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

    }

    /**
     * Open phone gallery
     */
    private void selectPicture() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }
}
