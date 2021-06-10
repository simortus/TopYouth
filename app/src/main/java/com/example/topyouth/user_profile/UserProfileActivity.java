package com.example.topyouth.user_profile;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.topyouth.R;
import com.example.topyouth.utility_classes.BottomNavigationHandler;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    //view
    private BottomNavigationView bottomNavigationView;
    private CircularImageView profileImage;
    private TextView status;
    //navigation handlers
    private BottomNavigationHandler bottomNavigationHandler;
    private Context context;

    //firebase
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth auth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        context = getApplicationContext();
        connectFirebase();
        widgets();

        bottomNavigationHandler = new BottomNavigationHandler(context, bottomNavigationView);
        bottomNavigationHandler.navigation();
    }

    private void connectFirebase() {
        authSingleton = FirebaseAuthSingleton.getInst(context);
        auth = authSingleton.mAuth();
        mUser = authSingleton.getCurrentUser();
    }


    private void widgets() {
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        status = findViewById(R.id.user_status);
        profileImage = findViewById(R.id.profilePic);
        setStatus(status);
    }

    private void setUserInfo() {
        // TODO: 6/9/21 load photo, name email, etc

    }

    @SuppressLint("SetTextI18n")
    private void setStatus(TextView textView) {
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
                    textView.setText("Approved");
                    textView.setTextColor(getResources().getColor(R.color.green));
                } else {
                    textView.setText("Not Approved");
                    textView.setTextColor(getResources().getColor(R.color.red));
                }
            }
        });
    }
}
