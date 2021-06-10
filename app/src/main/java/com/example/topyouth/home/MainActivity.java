package com.example.topyouth.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.topyouth.R;
import com.example.topyouth.login.LoginActivity;
import com.example.topyouth.utility_classes.BottomNavigationHandler;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    //view
    private FloatingActionButton logOut;
    private RelativeLayout notApprovedLayout;
    private BottomNavigationView bottomNavigationView;

    //firebase
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    // context
    private Context mContext;
    private BottomNavigationHandler bottomNavigationHandler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        widgets();


        connectFirebase();
        buttonListeners();
        bottomNavigationHandler.isAdminApproved(mUser,notApprovedLayout);

    }

//    private void isAdminApproved(RelativeLayout layout) {
//        final String user_id = mUser.getUid();
//        DocumentReference not_approved_users = mFirestore.collection("app_us").document(user_id);
//        not_approved_users.addSnapshotListener((value, error) -> {
//            if (error == null) {
//                Log.d(TAG, "onEvent: Document value_id: " + value.getId());
//                Log.d(TAG, "onEvent: Document value_approved_status: " + value.get("status"));
//                final String status = (String) value.get("status");
//                Log.d(TAG, "isAdminApproved: status: " + status);
//                boolean iss = status.equalsIgnoreCase("approved");
//                if (iss) {
//                    layout.setVisibility(View.GONE);
//                } else
//                    layout.setVisibility(View.VISIBLE);
//            }
//        });
//
//    }

    private void widgets() {
        logOut = findViewById(R.id.floating_button);
        notApprovedLayout = findViewById(R.id.not_approved_layout);
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        bottomNavigationHandler = new BottomNavigationHandler(mContext,bottomNavigationView);
        bottomNavigationHandler.navigation();

    }

    private void buttonListeners() {
        logOut.setOnClickListener(this);
    }

    private void connectFirebase() {
        authSingleton = FirebaseAuthSingleton.getInst(mContext);
        mAuth = authSingleton.mAuth();
        mUser = authSingleton.getCurrentUser();
        mAuthStateListener = firebaseAuth -> {
            mAuth.addAuthStateListener(mAuthStateListener);
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.floating_button:
//                signOut();
                Log.d(TAG, "onClick: FloatingActionButton clicked");
                break;
        }
    }


    private void signOut() {
        mAuth.signOut();
        Toast.makeText(mContext, "Logged out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
}
