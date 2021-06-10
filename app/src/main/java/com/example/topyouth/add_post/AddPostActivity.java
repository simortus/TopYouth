package com.example.topyouth.add_post;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.topyouth.R;
import com.example.topyouth.utility_classes.BottomNavigationHandler;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddPostActivity extends AppCompatActivity {
    private static final String TAG = "AddPostActivity";

    //view
    private BottomNavigationView bottomNavigationView ;
    private RelativeLayout notApprovedLayout;
    //navigation handlers
    private BottomNavigationHandler bottomNavigationHandler;
    private Context context ;

    //firebase
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        context =this;
        widgets();
        connectFirebase();

        bottomNavigationHandler = new BottomNavigationHandler(context,bottomNavigationView);
        bottomNavigationHandler.navigation();
        bottomNavigationHandler.isAdminApproved(mUser,notApprovedLayout);

    }

    private void widgets() {
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        notApprovedLayout = findViewById(R.id.not_approved_layout);
    }



    private void connectFirebase() {
        authSingleton = FirebaseAuthSingleton.getInst(context);
        mAuth = authSingleton.mAuth();
        mUser = authSingleton.getCurrentUser();
        mAuthStateListener = firebaseAuth -> {
            mAuth.addAuthStateListener(mAuthStateListener);
        };
    }


}
