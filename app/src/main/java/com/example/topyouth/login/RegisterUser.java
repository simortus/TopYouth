package com.example.topyouth.login;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.topyouth.R;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.google.common.base.Utf8;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static androidx.core.content.ContextCompat.getColor;

public class RegisterUser extends Fragment implements View.OnClickListener {
    private static final String TAG = "RegisterUserFragment";

    // view
    private EditText email, pass, conf_pass;
    private Button register_button;
    private ProgressBar progressBar;
    private RelativeLayout loading_layout, login_layout;

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
        FirebaseApp.initializeApp(this.getContext());
        context = getContext();
        try {
            hashAlgo = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, "onCreate: hashAlgo Exception: "+e.getLocalizedMessage());
        }
        authSingleton = FirebaseAuthSingleton.getInst(context);
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

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < 8) {
                    register_button.setBackgroundColor(getResources().getColor(R.color.fade_grey));
                    register_button.setTextColor(getResources().getColor(R.color.grey));
                    register_button.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

//                byte[] pass_encoded = hashAlgo.digest(pass.getText().toString().getBytes());
//                byte[] conf_pass_encoded = hashAlgo.digest(conf_pass.getText().toString().getBytes());

                if (s.toString().length() >= 8) {
                    register_button.setTextColor(getResources().getColor(R.color.blue_darkish));
                    register_button.setBackground(getResources().getDrawable(R.drawable.button_design_selector_blue));
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
        progressBar = view.findViewById(R.id.progress_bar);
        loading_layout = view.findViewById(R.id.loading_layout);

        register_button.setOnClickListener(this);

    }

    private void registerWithEmail() {
        //Todo continue here when you have free time . its a good practice
        loading_layout.setVisibility(View.VISIBLE);
        String mail = email.getText().toString();
        String passWOrd = pass.getText().toString();
        String confirmPass = conf_pass.getText().toString();
        final byte[] passHash = hashAlgo.digest(passWOrd.getBytes());
        final byte[] confPassHash = hashAlgo.digest(confirmPass.getBytes());

        //checking if any is empty or pass doesn't match, the rest mAuth takes care of
        if (TextUtils.isEmpty(mail)) {
            email.setError("Required");
            Toast.makeText(getContext(), "Please type in a valid email", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(passWOrd)) {
            pass.setError("Required");
            Toast.makeText(getContext(), "Please chose password", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(confirmPass)) {
            conf_pass.setError("Required");
            Toast.makeText(getContext(), "Please confirm password", Toast.LENGTH_SHORT).show();
        } else if (!passWOrd.equals(confirmPass) && !passHash.equals(confPassHash)) {
            pass.setError("!");
            Toast.makeText(getContext(), "Error: Password must match confirm password. Try again", Toast.LENGTH_SHORT).show();
        } else {
            String p = new String(passHash);
            authSingleton.createUserWithEmailAndPassword(mail, p);
            loading_layout.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_register) {
            registerWithEmail();
        }
    }
}
