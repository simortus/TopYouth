package com.example.topyouth.home;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.topyouth.R;
import com.example.topyouth.login.LoginActivity;
import com.example.topyouth.molde.PostModel;
import com.example.topyouth.view_utils.BottomNavigationHandler;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,  AdapterView.OnItemSelectedListener  {
    private static final String TAG = "MainActivity";
    //view
    private FloatingActionButton logOut;
    private RelativeLayout notApprovedLayout, userLayout;
    private BottomNavigationView bottomNavigationView;

    //firebase
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    // context
    private Context mContext;
    private BottomNavigationHandler bottomNavigationHandler;

    //vars
    private final List<PostModel> postList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        widgets();

        connectFirebase();
        buttonListeners();
        bottomNavigationHandler.isAdminApproved(mUser, notApprovedLayout, userLayout);

    }

    private void widgets() {
        logOut = findViewById(R.id.floating_button);
        notApprovedLayout = findViewById(R.id.not_approved_layout);
        userLayout = findViewById(R.id.userLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        bottomNavigationHandler = new BottomNavigationHandler(mContext, bottomNavigationView);
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
            mAuth.addAuthStateListener(this.mAuthStateListener);
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.floating_button:
                signOut();
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemSelected: Item clicked: "+view.getId());

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        return;
    }
}
