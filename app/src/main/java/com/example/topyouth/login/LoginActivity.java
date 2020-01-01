package com.example.topyouth.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.topyouth.R;
import com.example.topyouth.home.MainActivity;
import com.example.topyouth.molde.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    //widgets
    private Button login, register;
    private EditText email, pass, reg_email, reg_pass, reg_Cpass;
    private TextView forgot_pass;
    private ProgressDialog progressDialog;

    //context
    private Context mContext;

    //firebase-auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mUser;

    //firebase-database
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private DatabaseReference user_ref;

    //vars
    private boolean isVerified;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mContext = getApplicationContext();

        connectFirebase();
        inintWidgets();
        buttonListeners();
    }

    private void connectFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        user_ref = mDatabase.getReference("users");
        if (user_ref.getKey()== null){
            mDatabase.getReference("users").push();
            user_ref = mDatabase.getReference("users");
        }
        myRef = mDatabase.getReference();
    }

    private void buttonListeners() {
        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    private void inintWidgets() {

        login = findViewById(R.id.loginButton);
        register = findViewById(R.id.registerButton);
        email = findViewById(R.id.email_field);
        pass = findViewById(R.id.pass_field);
        reg_email = findViewById(R.id.registerEmail_field);
        reg_pass = findViewById(R.id.registerPass_field);
        reg_Cpass = findViewById(R.id.registerCPass_field);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginButton:
                signInWithEmail();
                break;
            case R.id.registerButton:
                registerWithEmail();
                break;
        }
    }

    private void registerWithEmail() {
        //Todo continue here when you have free time . its a good practice
        String mail = reg_email.getText().toString();
        String passWOrd = reg_pass.getText().toString();
        String confirmPass = reg_Cpass.getText().toString();
        //checking if any is empty or pass doesn't match, the rest mAuth takes care of
        if (TextUtils.isEmpty(mail)) {
            reg_email.setError("Required");
            Toast.makeText(getApplicationContext(), "Please type in a valid email", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(passWOrd)) {
            reg_pass.setError("Required");
            Toast.makeText(getApplicationContext(), "Please chose password", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(confirmPass)) {
            reg_Cpass.setError("Required");
            Toast.makeText(getApplicationContext(), "Please confirm password", Toast.LENGTH_SHORT).show();
        } else if (!passWOrd.equals(confirmPass)) {
            reg_pass.setError("");
            reg_Cpass.setError("");
            Toast.makeText(mContext, "Error: Password must match confirm password. Try again", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Creating account...");
            progressDialog.setMessage("Please wait while your account is being created...");
            progressDialog.setIcon(R.mipmap.logo);
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            mAuth.createUserWithEmailAndPassword(mail, passWOrd).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendVerifyEmail();
                    mAuth.signOut();
                    progressDialog.dismiss();
                    Toast.makeText(mContext, "Registered successfully, check your email", Toast.LENGTH_SHORT).show();

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(mContext, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    mAuth.signOut(); // always sign out the user if something goes wrong
                }


            }).addOnFailureListener(e -> {
                Toast.makeText(mContext, "Registration error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: error: " + e.getLocalizedMessage());
            });
        }
    }

    //Todo continue here.
    private void sendVerifyEmail() {

        FirebaseUser user = mAuth.getCurrentUser();// check user
        if (mAuth != null && user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    isVerified = true;
                    mAuth.signOut();// need to sign out the user every time until he confirms email

                } else {
                    String error = task.getException().getMessage();// get error from fireBase
                    Toast.makeText(mContext, "Error: " + error, Toast.LENGTH_SHORT).show();
                    mAuth.signOut();// need to sign out the user every time until he confirms email
                }
            });
        }
    }

    private void verifyAccount() {

        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user.isEmailVerified()) {
                addAuthListener();
                addUserToDataBase();
                goHome();

            } else {
                // else we first sign out the user, until he checks his email then he can connect
                mAuth.signOut();
                Toast.makeText(getApplicationContext(), "Please verify your account.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Something went wrong..." + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth != null && mUser != null) {
            goHome();
        }
    }


    private void addAuthListener() {
        try {
            mAuthStateListener = firebaseAuth -> {
                    mAuthStateListener.onAuthStateChanged(firebaseAuth);

            };
        } catch (Exception e) {
            Log.d(TAG, "addAuthListener: error: " + e.getLocalizedMessage());
        }

    }

    private void addUserToDataBase() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        final String nodeID = user_ref.child(firebaseUser.getUid()).getKey();
        final String userId = firebaseUser.getUid();
        final String userMail = firebaseUser.getEmail();
        final User user = new User(userId, userMail, "no_name", "no_photo",
                "no_about", "no_profession");

        Query query = user_ref.
                orderByKey().equalTo(nodeID);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    addNewUser(user);
                }

//                if (dataSnapshot.hasChildren() && !dataSnapshot.getKey().contains(userId)){
//                    addNewUser(user);
//                    Log.d(TAG, "addUserToDataBase: User: " + user.toString() + "added successfully\n");
////                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
////                        if (!ds.getKey().equals(userId)) {
////                            addNewUser(user);
////                            Log.d(TAG, "addUserToDataBase: User: " + user.toString() + "added successfully\n");
////                            return;
////                        }
////                    }
//                }
//                else {
//                    user_ref.child(userId).setValue(user);
//                }
//                if (!dataSnapshot.getKey().contains(userId)) {
//                    Log.d(TAG, "onDataChange: datasnapshot: "+dataSnapshot.getKey());
//                    addNewUser(user);
//                    Log.d(TAG, "addUserToDataBase:  user successfully added" + userMail);
//                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "databaseError: " + databaseError.getMessage());
            }
        });

    }

    private void addNewUser(User user) {
        myRef.child("users")
                .child(user.getUserId())
                .setValue(user);
    }

    private void goHome() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }


    private void signInWithEmail() {

        String mail = email.getText().toString();
        String password = pass.getText().toString();
        // first check if our textFields aren't empty
        if (TextUtils.isEmpty(password) && TextUtils.isEmpty(mail)) {
            email.setError("Required.");
            pass.setError("Required.");
            Toast.makeText(getApplicationContext(), "Please type in email or phone", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Please chose password", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mail)) {
            email.setError("Required.");
            Toast.makeText(getApplicationContext(), "Please type in email or phone", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(password)) {
            email.setError("Required.");
            Toast.makeText(getApplicationContext(), "Please chose password", Toast.LENGTH_SHORT).show();
        } else {

            progressDialog.setTitle("Signing in");
            progressDialog.setMessage("Signing in, please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setIcon(R.mipmap.logo);
            progressDialog.show();

            // after checking, we try to login
            mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {
                // if sign in is successful
                if (task.isSuccessful()) {
                    verifyAccount(); // check if user is verified by email
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                progressDialog.dismiss();
            }).addOnCanceledListener(() -> {
                mAuth.signOut();
                Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        }
    }
}
