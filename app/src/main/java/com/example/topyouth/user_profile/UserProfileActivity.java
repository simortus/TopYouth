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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.topyouth.R;
import com.example.topyouth.login.LoginActivity;
import com.example.topyouth.molde.TopUser;
import com.example.topyouth.utility_classes.DBSingelton;
import com.example.topyouth.utility_classes.MediaStuff;
import com.example.topyouth.utility_classes.Traveler;
import com.example.topyouth.view_utils.BottomNavigationHandler;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.topyouth.utility_classes.MediaStuff.CAMERA_REQUEST;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UserProfileActivity";

    //view
    private BottomNavigationView bottomNavigationView;
    private CircularImageView profileImage;
    private TextView status, username, emailHeader;
    private Button saveButton;
    private Toolbar toolbar;
    private View nav_header_view;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;


    //navigation handlers
    private BottomNavigationHandler bottomNavigationHandler;
    private Context context;

    //firebase
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth auth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private FirebaseDatabase database;
    private DBSingelton dbSingelton;
    private FirebaseStorage storage;

    //vars
    private MediaStuff mediaStuff;
    private Executor executor = Executors.newCachedThreadPool();
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        context = getApplicationContext();
        mediaStuff = new MediaStuff(this);
        connectFirebase();
        widgets();

        bottomNavigationHandler = new BottomNavigationHandler(context, bottomNavigationView);
        bottomNavigationHandler.navigation();
    }

    private void connectFirebase() {
        authSingleton = FirebaseAuthSingleton.getInst(this);
        auth = authSingleton.mAuth();
        mUser = authSingleton.getCurrentUser();
        dbSingelton = DBSingelton.getInstance();
        database = dbSingelton.getDbInstance();
        storage = dbSingelton.getStorage();

    }


    private void widgets() {
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        status = findViewById(R.id.user_status);
        profileImage = findViewById(R.id.profilePic);
        username = findViewById(R.id.username);
        saveButton = findViewById(R.id.saveButton);
        profileImage.setOnClickListener(this);
        saveButton.setOnClickListener(this);


        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolBar);
        //heaDER_LAYOUT
        nav_header_view = mNavigationView.getHeaderView(0);
        emailHeader = nav_header_view.findViewById(R.id.textView_emailDisplay_header);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        DrawerArrowDrawable arrowDrawable = new DrawerArrowDrawable(context);
        arrowDrawable.setColor(getResources().getColor(R.color.blue_darkish));
        arrowDrawable.setSpinEnabled(true);
        arrowDrawable.mutate();
        toggle.setDrawerArrowDrawable(arrowDrawable);
        toggle.syncState();
        setUserInfo();

        mNavigationView.setOnClickListener(this);
        navigationViewClickListener();

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
        Runnable runnable1 = () -> setStatus(status);
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

    @SuppressLint("SetTextI18n")
    private void setStatus(TextView textView) {
        final String user_id = mUser.getUid();
        DocumentReference not_approved_users = mFirestore.collection("app_us").document(user_id);
        not_approved_users.addSnapshotListener((value, error) -> {
            if (value != null && error == null) {
                Log.d(TAG, "onEvent: Document value_id: " + value.getId());
                Log.d(TAG, "onEvent: Document value_approved_status: " + value.get("status"));
                final String status = (String) value.get("status");
                Log.d(TAG, "isAdminApproved: status: " + status);
                boolean iss = status.equalsIgnoreCase("approved");
                if (iss) {
                    textView.setText("Approved");
                    textView.setTextColor(getResources().getColor(R.color.green));
                } else {
                    textView.setText("Not Approved");
                    textView.setTextColor(getResources().getColor(R.color.red));
                }
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        boolean besoins = resultCode == RESULT_OK && requestCode == CAMERA_REQUEST;

        try {

            if (besoins) {
                mUri = mediaStuff.getmUri();
                Glide.with(this).load(mUri).centerCrop().into(profileImage);
                saveButton.setEnabled(true);
                saveButton.setVisibility(View.VISIBLE);
            } else if (resultCode == RESULT_OK) {

                mUri = data.getData();
                Glide.with(this).load(mUri).centerCrop().into(profileImage);
                saveButton.setEnabled(true);
                saveButton.setVisibility(View.VISIBLE);

            }
        } catch (Exception e) {
            Log.d(TAG, "onActivityResult: Exception: " + e.getLocalizedMessage());
            Toast.makeText(context, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveButton:
                uploadNow(mUri);
                break;

            case R.id.profilePic:
                mediaStuff.dialogChoice();
                break;
        }


    }

    private void uploadNow(@NonNull final Uri uri) {
        saveButton.setVisibility(View.GONE);
        saveButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading, please wait...");
        progressDialog.setIcon(R.mipmap.top_youth_logo_icone);
        progressDialog.setCanceledOnTouchOutside(false);

        final String currentUser = mUser.getUid();
        StorageReference ref = storage.getReference("users");
        progressDialog.show();
        ref.child(currentUser).putFile(uri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        final String url = task1.getResult().toString();
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
        Query query = userRef.child(user_id);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: exists" + dataSnapshot.exists());
                    ((Runnable) () -> userRef.child(user_id).child("photo").setValue(url).addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show())).run();
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
//                        menuItem.setChecked(true);
//                        new Handler().postDelayed(() -> menuItem.setChecked(false), 500);
//                        mDrawerLayout.closeDrawer(GravityCompat.START);

//                        saveUsername();
                        break;

                    case R.id.changeProfPictureItem:
                        Log.d(TAG, "navigationViewClickListener changeProfPictureItem ");
//                        menuItem.setChecked(true);
//                        new Handler().postDelayed(() -> menuItem.setChecked(false), 500);
//                        mDrawerLayout.closeDrawer(GravityCompat.START);
//                        optionDialog();
                        break;

                    case R.id.signOutItem:
                        Log.d(TAG, "navigationViewClickListener signOutItem ");
                        menuItem.setChecked(true);
//                        new Handler().postDelayed(() -> menuItem.setChecked(false), 500);
                        mDrawerLayout.closeDrawer(mNavigationView);
                        mDrawerLayout.clearFocus();
                        mNavigationView.getMenu().close();
                        showSignOutDialog();
                        break;

                    case R.id.resetPassItem:
                        Log.d(TAG, "navigationViewClickListener resetPassItem ");
//                        menuItem.setChecked(true);
//                        new Handler().postDelayed(() -> menuItem.setChecked(false), 500);
//                        mDrawerLayout.closeDrawer(GravityCompat.START);
//                        handlePasswordReset();
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

    private void showDeleteAccountDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
        wlp.windowAnimations = R.style.Animation_Design_BottomSheetDialog;
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        alertDialog.getWindow().setAttributes(wlp);
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.round_edge_rectangle_button_recycler_view));

        View layoutView = getLayoutInflater().inflate(R.layout.layout_signout_dialog, null);
        TextView dialogTitle = layoutView.findViewById(R.id.dialogTitleTextView);
        Button confirmButton = layoutView.findViewById(R.id.confirmButton);
        Button cancelButton = layoutView.findViewById(R.id.cancelButton);
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
            final Query userQuery = dbSingelton.getUsers_ref().child(uid);
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
            final StorageReference storageReference = dbSingelton.getStorage().getReference("users").child(uid);
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
        wlp.windowAnimations = R.style.Animation_Design_BottomSheetDialog;
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        alertDialog.getWindow().setAttributes(wlp);
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.round_edge_rectangle_button_recycler_view));

        View layoutView = getLayoutInflater().inflate(R.layout.layout_signout_dialog, null);
        Button confirmButton = layoutView.findViewById(R.id.confirmButton);
        Button cancelButton = layoutView.findViewById(R.id.cancelButton);
        alertDialog.setView(layoutView);

        confirmButton.setOnClickListener(v -> {
            authSingleton.signOut();
            alertDialog.dismiss();
            new Traveler().gotoWithFlags(context, LoginActivity.class);
            finish();
        });
        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });


        alertDialog.show();


    }
}