package com.example.topyouth.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.topyouth.R;
import com.example.topyouth.login.LoginActivity;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //view
    private Button logOut ;

    //firebase
    private FirebaseAuth mAuth ;
    private FirebaseUser mUser ;
    private FirebaseAuth.AuthStateListener mAuthStateListener ;

    // context
    private Context mContext ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        widgets();
        connectFirebase();
        buttonListeners();


    }

    private void widgets() {
        logOut = findViewById(R.id.home_logOut);
    }

    private void buttonListeners() {
        logOut.setOnClickListener(this);
    }

    private void connectFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mAuthStateListener = firebaseAuth -> {
            mAuth.addAuthStateListener(mAuthStateListener);
        };

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.home_logOut:
                signOut();
                break;
        }
    }

    private void signOut() {
        mAuth.signOut();
        Toast.makeText(mContext,"Logged out",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
}
