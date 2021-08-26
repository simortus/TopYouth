package com.example.topyouth.login;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.topyouth.R;
import com.example.topyouth.auth_database.FirebaseAuthSingleton;
import com.example.topyouth.home.MainActivity;
import com.example.topyouth.utility_classes.Toaster;
import com.example.topyouth.utility_classes.Traveler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    //widgets
    private Button button_login;
    private TextView register_new_user;
    private EditText et_email, et_pass;
    private TextView forgot_pass;
    private RelativeLayout login_layout;

    //context
    private Context mContext;
    private FragmentManager fragmentManager;

    //firebase-auth
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    //utils
    private Traveler traveler = new Traveler();
    private MessageDigest hashAlgo;
    private AccessibilityService.SoftKeyboardController keyboardController;
    private Toaster toaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mContext = getApplicationContext();
        toaster = new Toaster(this);
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
                } else button_login.setEnabled(true);
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
        authSingleton = FirebaseAuthSingleton.getInst(this);
        if (authSingleton.checkInternetConnection()) {
            mAuth = authSingleton.mAuth();
            mUser = authSingleton.getCurrentUser();
            if (mAuth != null && authSingleton.isUserCompliant(mUser)) {
                traveler.gotoWithFlags(this, MainActivity.class);
            } else {
                authSingleton.signOut();
            }
        } else {
            toaster.displayNoConnectionMessage();
        }


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
        login_layout = findViewById(R.id.login_layout);

        register_new_user.setPaintFlags(register_new_user.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgot_pass.setPaintFlags(forgot_pass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login:
                signIn();
                break;

            case R.id.textViewforgot_pass:
                traveler.goFragment(fragmentManager, new ForgotPasswordFragment(), R.id.container);
                login_layout.setVisibility(View.GONE);
                break;

            case R.id.textView_register_new_user:
                // TODO: 6/6/21 open register fragment
                traveler.goFragment(fragmentManager, new RegisterUser(), R.id.container);
                login_layout.setVisibility(View.GONE);
                break;
        }
    }

    private void signIn() {
        final String email = et_email.getText().toString();
        final String pass = et_pass.getText().toString();
        final byte[] passEncoded = hashAlgo.digest(pass.getBytes());
        final String p = new String(passEncoded);
        traveler.hideKeyboard(getCurrentFocus(), mContext);
        if (authSingleton.checkInternetConnection()) {

            if (!email.isEmpty() && !pass.isEmpty()) {
                authSingleton.signInWithEmailAndPassword(email, p);
            } else
                Toast.makeText(mContext, "Please provide the needed credentials.", Toast.LENGTH_SHORT).show();
        } else {
            toaster.displayNoConnectionMessage();
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
    }
}
