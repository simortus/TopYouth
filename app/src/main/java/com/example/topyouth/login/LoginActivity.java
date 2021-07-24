package com.example.topyouth.login;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.topyouth.R;
import com.example.topyouth.home.MainActivity;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.example.topyouth.utility_classes.Traveler;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.example.topyouth.utility_classes.PasswordClassStuff.hasDigits;
import static com.example.topyouth.utility_classes.PasswordClassStuff.hasSpecial;
import static com.example.topyouth.utility_classes.PasswordClassStuff.isLongEnough;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    //widgets
    private Button button_login;
    private TextView register_new_user;
    private EditText et_email, et_pass;
    private TextView forgot_pass;
    private ProgressBar progressBar;
    private RelativeLayout loadingLayout, login_layout;

    //context
    private Context mContext;
    private MessageDigest hashAlgo;

    //firebase-auth
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    //utils
    private Traveler traveler = new Traveler();
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mContext = getApplicationContext();
        fragmentManager = getSupportFragmentManager();
        try {
            hashAlgo = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, "onCreate: Hash-Algorithm exception: " + e.getLocalizedMessage());
        }

        connectFirebase();
        inintWidgets();
        buttonListeners();

        et_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                button_login.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < 8) {
                    button_login.setEnabled(false);
                }
                else button_login.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() >= 8) {
                    button_login.setEnabled(true);
                }
            }
        });
    }

    private void connectFirebase() {
        authSingleton = FirebaseAuthSingleton.getInst(mContext);
        mAuth = authSingleton.mAuth();
        mUser = authSingleton.getCurrentUser();

    }

    private void buttonListeners() {
        button_login.setOnClickListener(this);
        register_new_user.setOnClickListener(this);
        forgot_pass.setOnClickListener(this);
    }

    private void inintWidgets() {
        button_login = findViewById(R.id.button_login);
        register_new_user = findViewById(R.id.textView_register_new_user);
        et_email = findViewById(R.id.email_field);
        et_pass = findViewById(R.id.pass_field);
        forgot_pass = findViewById(R.id.textViewforgot_pass);
        progressBar = findViewById(R.id.loadingBar);
        loadingLayout = findViewById(R.id.loading_layout);
        login_layout = findViewById(R.id.login_layout);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login:
                signIn();
                break;
            case R.id.textViewforgot_pass:
                // TODO: 6/3/21 open register fragmenet
                Toast.makeText(mContext, "textViewforgot_pass is clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.textView_register_new_user:
                // TODO: 6/6/21 open register fragment
                Toast.makeText(mContext, "textView_register_new_user is clicked", Toast.LENGTH_SHORT).show();
                traveler.goFragment(fragmentManager, new RegisterUser(), R.id.container);
                login_layout.setVisibility(View.INVISIBLE);

                break;
        }
    }

    private void signIn() {
        String email = et_email.getText().toString();
        String pass = et_pass.getText().toString();
        byte[] passEncoded = hashAlgo.digest(pass.getBytes());
        String p = new String(passEncoded);
        if (!email.isEmpty() && !pass.isEmpty()) {

            login_layout.setVisibility(View.INVISIBLE);
            loadingLayout.setVisibility(View.VISIBLE);
            Task<AuthResult> authResultTask = authSingleton.signInWithEmailAndPassword(email, p);
            if (!authResultTask.isSuccessful()) {
                login_layout.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.INVISIBLE);
            }

        }
        else {
            login_layout.setVisibility(View.VISIBLE);
            loadingLayout.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        login_layout.setVisibility(View.VISIBLE);
        super.onBackPressed();
        Log.d(TAG, "onBackPressed: stack: " + fragmentManager.getBackStackEntryCount());
        traveler.removeFromStack(fragmentManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth != null && authSingleton.isUserCompliant(mUser)) {
            authSingleton.checkApproved();
        } else {
            authSingleton.signOut();
        }
    }


//    private void addAuthListener() {
//        try {
//            mAuthStateListener = firebaseAuth -> {
//                    mAuthStateListener.onAuthStateChanged(firebaseAuth);
//
//            };
//        } catch (Exception e) {
//            Log.d(TAG, "addAuthListener: error: " + e.getLocalizedMessage());
//        }
//
//    }

//    private void addUserToDataBase() {
//        FirebaseUser firebaseUser = mAuth.getCurrentUser();
//        final String nodeID = user_ref.child(firebaseUser.getUid()).getKey();
//        final String userId = firebaseUser.getUid();
//        final String userMail = firebaseUser.getEmail();
//        final User user = new User(userId, userMail, "no_name", "no_photo",
//                "no_about", "no_profession");
//
//        Query query = user_ref.
//                orderByKey().equalTo(nodeID);
//
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()){
//                    addNewUser(user);
//                }
//
////                if (dataSnapshot.hasChildren() && !dataSnapshot.getKey().contains(userId)){
////                    addNewUser(user);
////                    Log.d(TAG, "addUserToDataBase: User: " + user.toString() + "added successfully\n");
//////                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
//////                        if (!ds.getKey().equals(userId)) {
//////                            addNewUser(user);
//////                            Log.d(TAG, "addUserToDataBase: User: " + user.toString() + "added successfully\n");
//////                            return;
//////                        }
//////                    }
////                }
////                else {
////                    user_ref.child(userId).setValue(user);
////                }
////                if (!dataSnapshot.getKey().contains(userId)) {
////                    Log.d(TAG, "onDataChange: datasnapshot: "+dataSnapshot.getKey());
////                    addNewUser(user);
////                    Log.d(TAG, "addUserToDataBase:  user successfully added" + userMail);
////                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "databaseError: " + databaseError.getMessage());
//            }
//        });
//
//    }

//    private void addNewUser(User user) {
//        myRef.child("users")
//                .child(user.getUserId())
//                .setValue(user);
//    }


}
