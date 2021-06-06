package com.example.topyouth.utility_classes;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.topyouth.R;
import com.google.firebase.auth.FirebaseAuth;

public class AuthOperations {




//    private void signInWithEmail(@NonNull String mail, @NonNull String password) {
//
//        // first check if our textFields aren't empty
//        if (TextUtils.isEmpty(password) && TextUtils.isEmpty(mail)) {
//            Toast.makeText(getApplicationContext(), "Please type in email or phone", Toast.LENGTH_SHORT).show();
//            Toast.makeText(getApplicationContext(), "Please chose password", Toast.LENGTH_SHORT).show();
//        } else if (TextUtils.isEmpty(mail)) {
//            email.setError("Required.");
//            Toast.makeText(getApplicationContext(), "Please type in email or phone", Toast.LENGTH_SHORT).show();
//
//        } else if (TextUtils.isEmpty(password)) {
//            email.setError("Required.");
//            Toast.makeText(getApplicationContext(), "Please chose password", Toast.LENGTH_SHORT).show();
//        } else {
//
//            progressDialog.setTitle("Signing in");
//            progressDialog.setMessage("Signing in, please wait...");
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.setIcon(R.mipmap.logo);
//            progressDialog.show();
//
//            // after checking, we try to login
//            mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {
//                // if sign in is successful
//                if (task.isSuccessful()) {
//                    verifyAccount(); // check if user is verified by email
//                    progressDialog.dismiss();
//                }
//            }).addOnFailureListener(e -> {
//                Toast.makeText(getApplicationContext(), "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                mAuth.signOut();
//                progressDialog.dismiss();
//            }).addOnCanceledListener(() -> {
//                mAuth.signOut();
//                Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_SHORT).show();
//                progressDialog.dismiss();
//            });
//        }
//    }
}
