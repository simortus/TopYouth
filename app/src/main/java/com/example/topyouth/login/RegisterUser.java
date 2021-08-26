package com.example.topyouth.login;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.topyouth.R;
import com.example.topyouth.auth_database.FirebaseAuthSingleton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.example.topyouth.utility_classes.PasswordClassStuff.hasDigits;
import static com.example.topyouth.utility_classes.PasswordClassStuff.hasSpecial;
import static com.example.topyouth.utility_classes.PasswordClassStuff.isLongEnough;

public class RegisterUser extends Fragment implements View.OnClickListener {
    private static final String TAG = "RegisterUserFragment";

    // view
    private EditText email, pass, conf_pass;
    private Button register_button;

    //context
    private Context context;

    // sha and crypto
    private MessageDigest hashAlgo;

    //auth and db
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth mAuth;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        assert context != null;
        FirebaseApp.initializeApp(context);
        try {
            hashAlgo = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, "onCreate: hashAlgo Exception: " + e.getLocalizedMessage());
        }
        authSingleton = FirebaseAuthSingleton.getInst(this.getActivity());
        mAuth = authSingleton.mAuth();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_activity_layout, null);
        findWidgets(view);
        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                register_button.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < 8) {
                    register_button.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().length() >= 8) {
                    register_button.setEnabled(true);
                }
            }
        });
        return view;

    }

    private void findWidgets(View view) {
        email = view.findViewById(R.id.email_field);
        pass = view.findViewById(R.id.pass_field);
        conf_pass = view.findViewById(R.id.registerCPass_field);
        register_button = view.findViewById(R.id.button_register);
        register_button.setOnClickListener(this);

    }

    private void registerWithEmail() {
        //Todo continue here when you have free time . its a good practice
        final String mail = email.getText().toString();
        final String passWOrd = pass.getText().toString();
        final String confirmPass = conf_pass.getText().toString();
        final byte[] passHash = hashAlgo.digest(passWOrd.getBytes());
        final byte[] confPassHash = hashAlgo.digest(confirmPass.getBytes());

        if (!TextUtils.isEmpty(mail) && !TextUtils.isEmpty(passWOrd) && !TextUtils.isEmpty(confirmPass)) {
            if (isLongEnough(passWOrd) && hasDigits(passWOrd) && hasSpecial(passWOrd)) {
                if (passWOrd.equals(confirmPass) && Arrays.equals(passHash, confPassHash)) {
                    Log.d(TAG, "registerWithEmail: everything is ok");
                    final String p = new String(passHash);
                    authSingleton.createUserWithEmailAndPassword(mail, p);

                } else {
                    Toast.makeText(getContext(), "Error: Password must match confirm password. Try again", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(context, "Please chose a strong password, with digits, uppercase letter and special characters", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "All fields are required. Try again", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_register) {
            registerWithEmail();
        }
    }
}
