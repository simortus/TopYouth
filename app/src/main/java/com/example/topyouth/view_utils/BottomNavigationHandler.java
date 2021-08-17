package com.example.topyouth.view_utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.example.topyouth.R;
import com.example.topyouth.add_post.AddPostActivity;
import com.example.topyouth.home.MainActivity;
import com.example.topyouth.user_profile.UserProfileActivity;
import com.example.topyouth.utility_classes.Traveler;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BottomNavigationHandler extends BottomNavigationView {
    private static final String TAG = "BottomNavigationHandler";
    private BottomNavigationView navigationView;
    private Activity context;
    private Traveler traveler = new Traveler();

    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    public BottomNavigationHandler(@NonNull Activity context, BottomNavigationView bnv) {
        super(context);
        this.context = context;
        this.navigationView = bnv;
        navigationView.setItemHorizontalTranslationEnabled(true);
    }

    @SuppressLint("NonConstantResourceId")
    public void navigation() {
        navigationView.setOnNavigationItemSelectedListener(item -> {
            final String activityClassNAme = context.getLocalClassName();
            Log.d(TAG, "navigation: activityClassNAme: "+activityClassNAme);
            switch (item.getItemId()) {
                case R.id.home_icone:
                    navigationView.getMenu().getItem(0).setIcon(R.drawable.home_selected);
                    item.getIcon().setTint(getResources().getColor(R.color.green));
                    if (!activityClassNAme.contains("home.MainActivity")) {
                        traveler.gotoWithFlags(context, MainActivity.class);

                    }

                    break;
                case R.id.addPost:
                    navigationView.getMenu().getItem(1).setIcon(R.drawable.add_plus_icone);
                    item.getIcon().setTint(getResources().getColor(R.color.green));
                    if (!activityClassNAme.contains("add_post.AddPostActivity")) {
                        traveler.gotoWithFlags(context, AddPostActivity.class);

                    }

                    break;
                case R.id.profile:
                    navigationView.getMenu().getItem(2).setIcon(R.drawable.user);
                    item.getIcon().setTint(getResources().getColor(R.color.green));
                    if (!activityClassNAme.contains("user_profile.UserProfileActivity")) {
                        traveler.gotoWithFlags(context, UserProfileActivity.class);
                    }

                    break;

            }
            return true;
        });
    }

    public void isAdminApproved(@NonNull final FirebaseUser mUser, @NonNull final RelativeLayout notApprovedLayout, @NonNull final RelativeLayout userLayout) {
        ExecutorService executors = Executors.newCachedThreadPool();
        Runnable isApproved = () -> {
            try {
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
                            notApprovedLayout.setVisibility(View.GONE);
                            userLayout.setVisibility(View.VISIBLE);
                            userLayout.setClickable(false);
                            userLayout.setFocusable(false);
                        } else {
                            notApprovedLayout.setVisibility(View.VISIBLE);
                            userLayout.setVisibility(View.GONE);
                            userLayout.setClickable(true);
                            userLayout.setFocusable(true);
                        }
                    }
                });

            } catch (Exception e) {
                Log.d(TAG, "isAdminApproved_Exception: " + e.getLocalizedMessage());
            }
        };
        synchronized (this) {
            try {
                this.wait(500);
                executors.execute(isApproved);
            } catch (InterruptedException e) {
                Log.d(TAG, "isAdminApproved: Exeception: " + e.getMessage());
            }
        }
    }



//        public boolean isAdminApproved(@NonNull final FirebaseUser mUser,
//                                   @NonNull final RelativeLayout notApprovedLayout,
//                                   @NonNull final RelativeLayout userLayout) {
//       AtomicBoolean is = new AtomicBoolean(false);
//        ExecutorService executors = Executors.newCachedThreadPool();
//        Runnable isApproved = () -> {
//            try {
//                final String user_id = mUser.getUid();
//                DocumentReference not_approved_users = mFirestore.collection("app_us").document(user_id);
//                not_approved_users.addSnapshotListener((value, error) -> {
//                    if (value != null && error == null) {
//                        Log.d(TAG, "onEvent: Document value_id: " + value.getId());
//                        Log.d(TAG, "onEvent: Document value_approved_status: " + value.get("status"));
//                        final String status = (String) value.get("status");
//                        Log.d(TAG, "isAdminApproved: status: " + status);
//                        boolean iss = status.equalsIgnoreCase("approved");
//                        if (iss) {
//                            notApprovedLayout.setVisibility(View.GONE);
//                            userLayout.setVisibility(View.VISIBLE);
//                            userLayout.setClickable(false);
//                            userLayout.setFocusable(false);
//                            is.set(true);
//                        } else {
//                            notApprovedLayout.setVisibility(View.VISIBLE);
//                            userLayout.setVisibility(View.GONE);
//                            userLayout.setClickable(true);
//                            userLayout.setFocusable(true);
//                        }
//                    }
//                });
//
//            } catch (Exception e) {
//                Log.d(TAG, "isAdminApproved_Exception: " + e.getLocalizedMessage());
//            }
//        };
//        synchronized (this) {
//            try {
//                this.wait(500);
//                executors.execute(isApproved);
//            } catch (InterruptedException e) {
//                Log.d(TAG, "isAdminApproved: Exeception: " + e.getMessage());
//            }
//        }
//
//        return is.get();
//
//    }

}