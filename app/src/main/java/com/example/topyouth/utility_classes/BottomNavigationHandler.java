package com.example.topyouth.utility_classes;

import android.content.Context;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.topyouth.R;
import com.example.topyouth.add_post.AddPostActivity;
import com.example.topyouth.home.MainActivity;
import com.example.topyouth.user_profile.UserProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BottomNavigationHandler {
    private static final String TAG = "BottomNavigationHandler";
    private BottomNavigationView bottomNavigationViewEx;
    private Context context;
    private Traveler traveler = new Traveler();

    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    public BottomNavigationHandler(@NonNull Context context, BottomNavigationView bnv) {
        this.context = context;
        this.bottomNavigationViewEx = bnv;
        bottomNavigationViewEx.setItemHorizontalTranslationEnabled(true);
//        bottomNavigationViewEx.animate();
    }

    public void navigation() {
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home_icone:
                    Log.d(TAG, "onNavigationItemSelected: home");
//                    item.setChecked(true);
//                    item.setEnabled(true);

                    if (!context.getClass().equals("MainActivity")){
                        traveler.gotoWithFlags(context, MainActivity.class);
                    }

                    break;
                case R.id.addPost:
                    Log.d(TAG, "onNavigationItemSelected: addPost");
//                    item.setChecked(true);
//                    item.setEnabled(true);
                    item.getIcon().setTint(context.getResources().getColor(R.color.blue_darkish));
                    item.collapseActionView();

                    if (!context.getClass().equals("AddPostActivity")){
                        traveler.gotoWithFlags(context, AddPostActivity.class);
                    }
                    break;
                case R.id.profile:
                    Log.d(TAG, "onNavigationItemSelected: profile");
//                    item.setChecked(true);
//                    item.setEnabled(true);

                    if (!context.getClass().equals("UserProfileActivity")){
                        traveler.gotoWithFlags(context, UserProfileActivity.class);
                    }
                    break;

            }
            return false;
        });
    }

    public void isAdminApproved(@NonNull final FirebaseUser mUser , @NonNull final RelativeLayout layout) {
        final String user_id = mUser.getUid();
        DocumentReference not_approved_users = mFirestore.collection("app_us").document(user_id);
        not_approved_users.addSnapshotListener((value, error) -> {
            if (error == null) {
                Log.d(TAG, "onEvent: Document value_id: " + value.getId());
                Log.d(TAG, "onEvent: Document value_approved_status: " + value.get("status"));
                final String status = (String) value.get("status");
                Log.d(TAG, "isAdminApproved: status: " + status);
                boolean iss = status.equalsIgnoreCase("approved");
                if (iss) {
                    layout.setVisibility(View.GONE);
                } else
                    layout.setVisibility(View.VISIBLE);
            }
        });

    }
}