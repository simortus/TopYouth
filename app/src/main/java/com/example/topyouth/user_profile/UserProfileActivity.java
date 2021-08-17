package com.example.topyouth.user_profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.topyouth.R;
import com.example.topyouth.login.LoginActivity;
import com.example.topyouth.molde.PostModel;
import com.example.topyouth.molde.TopUser;
import com.example.topyouth.utility_classes.DBSingleton;
import com.example.topyouth.utility_classes.MediaStuff;
import com.example.topyouth.utility_classes.Traveler;
import com.example.topyouth.view_utils.BottomNavigationHandler;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.example.topyouth.view_utils.GridImageAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.topyouth.utility_classes.MediaStuff.CAMERA_REQUEST;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UserProfileActivity";
    private static final int NUM_GRID_COLUMNS = 3;

    //view
    private BottomNavigationView bottomNavigationView;
    private CircularImageView profileImage, approved_sign;
    private TextView username, emailHeader;
    private Toolbar toolbar;
    private View nav_header_view;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private GridView gridView;


    //navigation handlers
    private BottomNavigationHandler bottomNavigationHandler;
    private Context context;
    private Activity currentActvitiy;
    private GridImageAdapter adapter;

    //firebase
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth auth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private FirebaseDatabase database;
    private DBSingleton dbSingleton;
    private FirebaseStorage storage;

    //vars
    private MediaStuff mediaStuff;
    private Executor executor = Executors.newCachedThreadPool();
    private Uri mUri;
    private List<PostModel> postModelList = new ArrayList<>();
    private boolean updateImage, updateName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        context = getApplicationContext();
        currentActvitiy = this;
        mediaStuff = new MediaStuff(this);
        connectFirebase();
        widgets();
        setupGridView();
    }

    private void connectFirebase() {
        authSingleton = FirebaseAuthSingleton.getInst(this);
        auth = authSingleton.mAuth();
        mUser = authSingleton.getCurrentUser();
        dbSingleton = DBSingleton.getInstance();
        database = dbSingleton.getDbInstance();
        storage = dbSingleton.getStorage();

    }


    private void widgets() {
        //findView by ID
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        profileImage = findViewById(R.id.profilePic);
        approved_sign = findViewById(R.id.approve_image_sign);
        username = findViewById(R.id.username);
        gridView = findViewById(R.id.gridView);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolBar);

        adapter = new GridImageAdapter(context, R.layout.layout_grid_imageview, postModelList);
        gridView.setAdapter(adapter);

        //bottomNav handlers
        bottomNavigationHandler = new BottomNavigationHandler(currentActvitiy, bottomNavigationView);
        bottomNavigationHandler.navigation();
        MenuItem userItem = bottomNavigationView.getMenu().getItem(2);
        userItem.setChecked(true);


        //heaDER_LAYOUT
        nav_header_view = mNavigationView.getHeaderView(0);
        emailHeader = nav_header_view.findViewById(R.id.textView_emailDisplay_header);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        DrawerArrowDrawable arrowDrawable = new DrawerArrowDrawable(context);
        arrowDrawable.setColor(getResources().getColor(R.color.green));
        arrowDrawable.setSpinEnabled(true);
        arrowDrawable.mutate();
        toggle.setDrawerArrowDrawable(arrowDrawable);
        toggle.syncState();
        setUserInfo();

        mNavigationView.setOnClickListener(this);
        profileImage.setOnClickListener(this);
        navigationViewClickListener();

        gridviewClickListener();

    }

    // handles the gridView onItemClickListener events
    private void gridviewClickListener() {
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(context, "Item clicked url: " + postModelList.get(position).getPostDetails() + id, Toast.LENGTH_SHORT).show();
            PostModel currentPostModel = postModelList.get(position);
            displayPostInDialog(currentPostModel);
        });
    }

    private void displayPostInDialog(@NonNull PostModel currentPostModel) {

        // TODO: 8/3/21 finish this one here
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        AlertDialog alertDialog = alertDialogBuilder.create();
//        alertDialog.setCanceledOnTouchOutside(false);
//        WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
//        wlp.windowAnimations = R.style.Animation_Design_BottomSheetDialog;
//        wlp.gravity = Gravity.BOTTOM;
//        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        alertDialog.getWindow().setAttributes(wlp);
//        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.round_edge_rectangle_button_recycler_view));
//
//        View layoutView = getLayoutInflater().inflate(R.layout.layout_signout_dialog, null);
//        Button confirmButton = layoutView.findViewById(R.id.confirmButton);
//        Button cancelButton = layoutView.findViewById(R.id.cancelButton);
//        alertDialog.setView(layoutView);
//
//        confirmButton.setOnClickListener(v -> {
//            authSingleton.signOut();
//            alertDialog.dismiss();
//            new Traveler().gotoWithFlags(this, LoginActivity.class);
//            finish();
//        });
//        cancelButton.setOnClickListener(v -> {
//            alertDialog.dismiss();
//        });
//
//
//        alertDialog.show();
    }


    /**
     *
     **/
    private void setupGridView() {
        Runnable gridViewRunnable = () -> {
            try {
                final String user_id = mUser.getUid();
                final DatabaseReference thisUserPosts = dbSingleton.getPostsRef();

                final Query postsQuery = thisUserPosts.child(user_id);
                postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "setupGridView onDataChange: datasnapshot.key: " + dataSnapshot.getKey());
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            PostModel postModel = ds.getValue(PostModel.class);
                            Log.d(TAG, "setupGridView onDataChange: " + postModel.getPostDetails());
                            postModelList.add(postModel);

                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "setupGridView: error; " + databaseError.getMessage());//<-catch exception to prevent app crush -->

                    }
                });

            } catch (Exception e) {
                Log.d(TAG, "setupGridView: error; " + e.getMessage());//<-catch exception to prevent app crush -->
            }
        };
        if (authSingleton.checkInternetConnection()) {
            executor.execute(gridViewRunnable);
        } else {
            Toast.makeText(context, R.string.check_internet, Toast.LENGTH_SHORT).show();//<-Notify user to check internet-->
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();//exits if back pressed
        //handling for the drawerLayout so we exit it instead of exiting the activity
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }

    }


    private void setUserInfo() {
        final DatabaseReference userRef = database.getReference("users");
        final String user_id = mUser.getUid();
        Runnable runnable1 = this::setStatus;
        Runnable runnable2 = () -> {
            Query query = userRef.child(user_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: exists");
                        TopUser user = dataSnapshot.getValue(TopUser.class);
                        Log.d(TAG, "onDataChange: currentUserId: " + user.getUserId());
                        Log.d(TAG, "onDataChange: currentUserId: " + user_id);

                        username.setText(user.getUsername());
                        if (!user.getPhoto().equals("no_photo")) {
                            Glide.with(context).load(user.getPhoto()).centerCrop().into(profileImage);
                            emailHeader.setText(user.getEmail());
                        } else
                            profileImage.setBackground(getResources().getDrawable(R.drawable.user));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: Database error: " + databaseError.getMessage());

                }
            });
        };


        executor.execute(runnable1);
        executor.execute(runnable2);


    }

    private void setStatus() {
        final String user_id = mUser.getUid();
        DocumentReference not_approved_users = mFirestore.collection("app_us").document(user_id);
        not_approved_users.addSnapshotListener((value, error) -> {
            if (value != null && error == null) {
                Log.d(TAG, "onEvent: Document value_id: " + value.getId());
                Log.d(TAG, "onEvent: Document value_approved_status: " + value.get("status"));
                final String status = (String) value.get("status");
                Log.d(TAG, "isAdminApproved: status: " + status);
                assert status != null;
                boolean iss = status.equalsIgnoreCase("approved");
                if (iss) {
                    approved_sign.setVisibility(View.VISIBLE);
                } else {
                    approved_sign.setVisibility(View.GONE);
                }
            } else
                approved_sign.setVisibility(View.GONE);

        });
    }


    private void uploadNow(@NonNull final Uri uri) {
//        saveButton.setVisibility(View.GONE);
//        saveButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading, please wait...");
        progressDialog.setIcon(R.mipmap.top_youth_logo_icone);
        progressDialog.setCanceledOnTouchOutside(false);

        final String currentUser = mUser.getUid();
        final StorageReference ref = storage.getReference("users");
        progressDialog.show();

        ref.child(currentUser).putFile(uri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && task.getResult() != null) {
                        final String url = task1.getResult().toString();
                        Glide.with(context).load(uri).centerCrop().into(profileImage);
                        updateUserImage(url);
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

    private void updateUserImage(@NonNull final String url) {
        final String user_id = mUser.getUid();
        final DatabaseReference userRef = database.getReference("users");
        final Query query = userRef.child(user_id);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: exists" + dataSnapshot.exists());
                    ((Runnable) () -> userRef.child(user_id).child("photo").setValue(url).addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Profile picture updated successfully", Toast.LENGTH_SHORT).show())).run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Database error: " + databaseError.getMessage());
                Toast.makeText(context, "Error updating your profile picture, ", Toast.LENGTH_SHORT).show();

            }
        });

    }

    /**
     * @use navigation view clickListener
     **/
    private void navigationViewClickListener() {
        try {
            mNavigationView.setNavigationItemSelectedListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.chengeUserNameItem:
                        Log.d(TAG, "navigationViewClickListener chengeUserNameItem ");
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        mDrawerLayout.clearFocus();
                        mNavigationView.getMenu().close();
                        updateName = true;
                        updateImage = false;
                        changeUsername();
                        break;

                    case R.id.changeProfPictureItem:
                        Log.d(TAG, "navigationViewClickListener changeProfPictureItem ");
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        mDrawerLayout.clearFocus();
                        mNavigationView.getMenu().close();
                        updateName = false;
                        updateImage = true;
                        mediaStuff.dialogChoice();
                        break;

                    case R.id.signOutItem:
                        Log.d(TAG, "navigationViewClickListener signOutItem ");
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        mDrawerLayout.clearFocus();
                        mNavigationView.getMenu().close();
                        showSignOutDialog();
                        break;

                    case R.id.resetPassItem:
                        Log.d(TAG, "navigationViewClickListener resetPassItem ");
                        break;

                    case R.id.delete_accountItem:
                        Log.d(TAG, "navigationViewClickListener delete_accountItem ");
                        menuItem.setChecked(true);
                        new Handler().postDelayed(() -> menuItem.setChecked(false), 500);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        showDeleteAccountDialog();
                        break;
                }
                return false;
            });
        } catch (Exception e) {
            Log.d(TAG, "navigationViewClickListener: " + e.getLocalizedMessage());
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profilePic:
                mediaStuff.dialogChoice();
                break;
        }


    }


    private void changeUsername() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        final WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
        wlp.windowAnimations = R.style.Animation_Design_BottomSheetDialog;
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        alertDialog.getWindow().setAttributes(wlp);
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.round_edge_rectangle_button_recycler_view));

        final View layoutView = getLayoutInflater().inflate(R.layout.change_username_dialog_layout, null);
        final EditText editTextUsername = layoutView.findViewById(R.id.editText_username);
        final Button confirmButton = layoutView.findViewById(R.id.saveButton);
        final Button cancelButton = layoutView.findViewById(R.id.discardButton);
        alertDialog.setView(layoutView);

        confirmButton.setOnClickListener(v -> {
            final String new_username = editTextUsername.getText().toString();
            if (!new_username.isEmpty()) {
                alertDialog.dismiss();
                saveUserName(new_username);
            } else {
                editTextUsername.setError("Please enter a valid username");
            }

        });
        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });


        alertDialog.show();
    }

    //tested and works fine
    private void saveUserName(@NonNull String new_username) {
        final DatabaseReference userRef = dbSingleton.getUsers_ref();
        final String userID = mUser.getUid();
        final Query query = userRef.child(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userRef.child(userID).child("username").setValue(new_username).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            username.setText(new_username);
                            Toast.makeText(context, "Username changed successfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e ->
                            Log.d(TAG, "saveUserName onFailure: exception: " + e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "saveUserName onFailure: databaseError: " + databaseError.getMessage());
            }
        });
    }

    private void showDeleteAccountDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);
        final WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
        wlp.windowAnimations = R.style.Animation_Design_BottomSheetDialog;
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        alertDialog.getWindow().setAttributes(wlp);
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.round_edge_rectangle_button_recycler_view));

        final View layoutView = getLayoutInflater().inflate(R.layout.layout_signout_dialog, null);
        final TextView dialogTitle = layoutView.findViewById(R.id.dialogTitleTextView);
        final Button confirmButton = layoutView.findViewById(R.id.confirmButton);
        final Button cancelButton = layoutView.findViewById(R.id.cancelButton);
        confirmButton.setText(R.string.delete_accnt);
        dialogTitle.setText(R.string.delet_accnt_warning);
        confirmButton.getDisplay();

        alertDialog.setView(layoutView);

        confirmButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            deleteAccnt();
        });
        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });


        alertDialog.show();
    }

    private void deleteAccnt() {
        final String uid = mUser.getUid();

        // delete user from realTime users node
        // delete user from storage
        // delete user from firestore, if not permitted, send a delete request in the firestore document set "delete_requests"
        // delete user from auth

        Runnable deleteFromUserNode = () -> {
            final Query userQuery = dbSingleton.getUsers_ref().child(uid);
            userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        dataSnapshot.getRef().removeValue().addOnCompleteListener(task -> {
                            Log.d(TAG, "onComplete: success: " + task.isSuccessful());
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: databaseError: " + databaseError.getMessage());
                }
            });

        };
        Runnable deleteFromStorage = () -> {
            final StorageReference storageReference = dbSingleton.getStorage().getReference("users").child(uid);
            storageReference.delete().addOnCompleteListener(task -> {
                Log.d(TAG, "onComplete: success: " + task.isSuccessful());

            }).addOnFailureListener(e -> {
                Log.d(TAG, "onCancelled: databaseError: " + e.getMessage());
            });

        };
        Runnable deleteFromAuth = () -> {

            mUser.delete().addOnCompleteListener(task -> {
                Log.d(TAG, "onComplete: success: " + task.isSuccessful());
                // TODO: 7/24/21 continue with th rest proceders, logout and go to LoginActivity

            }).addOnFailureListener(e -> {
                Log.d(TAG, "onCancelled: databaseError: " + e.getMessage());
            });

        };


//        executor.execute(deleteFromUserNode);
//        executor.execute(deleteFromStorage);
//        executor.execute(deleteFromAuth);
    }

    private void showSignOutDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        final WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
        wlp.windowAnimations = R.style.Animation_Design_BottomSheetDialog;
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        alertDialog.getWindow().setAttributes(wlp);
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.round_edge_rectangle_button_recycler_view));

        final View layoutView = getLayoutInflater().inflate(R.layout.layout_signout_dialog, null);
        final Button confirmButton = layoutView.findViewById(R.id.confirmButton);
        final Button cancelButton = layoutView.findViewById(R.id.cancelButton);
        alertDialog.setView(layoutView);

        confirmButton.setOnClickListener(v -> {
            authSingleton.signOut();
            alertDialog.dismiss();
            new Traveler().gotoWithFlags(this, LoginActivity.class);
            finish();
        });
        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        boolean condition = resultCode == RESULT_OK && requestCode == CAMERA_REQUEST;

        try {
            if (condition) {
                mUri = mediaStuff.getmUri();
                displayProfileChangeDialog(mUri);
            } else if (resultCode == RESULT_OK) {
                mUri = data.getData();
                displayProfileChangeDialog(mUri);
            }
        } catch (Exception e) {
            Log.d(TAG, "onActivityResult: Exception: " + e.getLocalizedMessage());
            Toast.makeText(context, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayProfileChangeDialog(@NonNull Uri mUri) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        final WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
        wlp.windowAnimations = R.style.Animation_Design_BottomSheetDialog;
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        alertDialog.getWindow().setAttributes(wlp);
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.round_edge_rectangle_button_recycler_view));

        final View layoutView = getLayoutInflater().inflate(R.layout.change_profile_picture_layout, null);
        final CircularImageView imageProfile = layoutView.findViewById(R.id.profilePicFrame);
        final Button confirmButton = layoutView.findViewById(R.id.saveButton);
        final Button cancelButton = layoutView.findViewById(R.id.discardButton);
        Glide.with(context).load(mUri).centerCrop().into(imageProfile);
        alertDialog.setView(layoutView);

        confirmButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            uploadNow(mUri);

        });
        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();

    }
}