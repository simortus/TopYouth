package com.example.topyouth.auth_database;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.topyouth.R;
import com.example.topyouth.home.MainActivity;
import com.example.topyouth.login.LoginActivity;
import com.example.topyouth.molde.TopUser;
import com.example.topyouth.utility_classes.Traveler;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FederatedAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author mohamed-msaad
 * Auth class used to override the Firebase auth methods and return only the needed functionalities
 **/
public class FirebaseAuthSingleton extends FirebaseAuth {
    private static final String TAG = "AuthSingleton";
    private final Activity mContext;
    //<<----- auth, DB---->>
    private final static FirebaseAuth myAuth = FirebaseAuth.getInstance();
    private static FirebaseUser mCurrentUser;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private final DBSingleton mDbSingleton = DBSingleton.getInstance();


    //vars
    private final Traveler mTraveler = new Traveler();
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    //<<------------------------------Begin methods--------------------------------->>
    private FirebaseAuthSingleton(@NonNull Activity activity) {
        super(FirebaseApp.getInstance());
        this.mContext = activity;
    }

    //singleton of this class
    @NonNull
    public static FirebaseAuthSingleton getInst(@NonNull Activity activity) {
        return new FirebaseAuthSingleton(activity);
    }

    public FirebaseAuth mAuth() {
        return myAuth;
    }

    public boolean isUserCompliant(@NonNull FirebaseUser currentUser) {
        return currentUser != null && currentUser.isEmailVerified() && !currentUser.isAnonymous();
    }

    public boolean checkInternetConnection() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isAvailable() &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }


    @Override
    public void signOut() {
        database.goOffline();
        mFirestore.disableNetwork();
        myAuth.signOut();
    }


    ////works like a charm
    //this is private and restricts the publicly offered login process :)
    private void verifyAccount(@NonNull FirebaseUser currentUser, @NonNull Context context, @NonNull final String provider) {
        try {
            if (isUserCompliant(currentUser) && provider.equals("password")) {

                Log.d(TAG, "verifyAccount: is email verified: " + currentUser.isEmailVerified());
                @NonNull final String email = currentUser.getEmail();
                @NonNull final String userUid = currentUser.getUid();
                addNewUser(email, userUid);

            } else {
                closeDial();
                signOut();//todo: Mo's notes: security stuff: close auth and databases connections when things are canceled or failed
                Toast.makeText(context, "Please verify your account.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            closeDial();
            Log.d(TAG, "verifyAccount: error: " + e.getMessage());
            Toast.makeText(context, "Something went wrong..." + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            signOut();
        }
    }

    //works like a charm
    //check if user exists in fire-store and adds it if not else go to main
    private void getDocumentForUSer(@NonNull final String userId, @NonNull final String email) {
        final Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        map.put("status", "not_approved");
        DocumentReference thisUserDoc = mFirestore.collection("app_us").document(userId);
        thisUserDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                createDocument(userId, email);
            }
            if (documentSnapshot.exists()) {
                Log.d(TAG, "getDocumentForUSer: documentSnapshot id: " + documentSnapshot.getId());
                //todo here we check if the shared pref is true for phone verified
                closeDial();
                mTraveler.gotoWithFlags(mContext, MainActivity.class);
            }
        }).addOnFailureListener(e -> {
            closeDial();
            Log.d(TAG, "onFailure: not worked: " + e.getMessage());
        });

    }

    public void checkApproved() {
        try {
            final String user_id = mCurrentUser.getUid();
            DocumentReference not_approved_users = mFirestore.collection("app_us").document(user_id);
            not_approved_users.addSnapshotListener((value, error) -> {
                if (value != null) {
                    if (!value.exists()) {
                        signOut();
                        mTraveler.gotoWithFlags(mContext, LoginActivity.class);
                    } else {
                        mTraveler.gotoWithFlags(mContext, MainActivity.class);
                    }
                }

            });
        } catch (Exception e) {
            Log.d(TAG, "checkApproved: Exception: " + e.getLocalizedMessage());
        }
    }

    //works like a charm
    private void createDocument(@NonNull final String userId, @NonNull final String email) {
        final Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        map.put("email", email);
        map.put("status", "not_approved");
        mFirestore.collection("app_us").document(userId).set(map).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "onSuccess: Successful operation: " + documentReference);
            closeDial();
            mTraveler.gotoWithFlags(mContext, MainActivity.class);

        }).addOnFailureListener(e ->{
            closeDial();
            Log.d(TAG, "onFailure: not worked: " + e.getMessage());
        });

    }

    // //  works like a charm
    private void addNewUser(@NonNull final String email, @NonNull final String user_id) {
        final String name = "no_name",
                profileUrl = "no_url",
                about = "no_details";
        //todo Mo's: this is to prevent malicious users from trying to skip the phone confirmation, by trying to add a phone number at this step
        final TopUser topUser = new TopUser(user_id, email, name, profileUrl, about);
        DatabaseReference user_ref = mDbSingleton.getUsers_ref();
        Query query = user_ref.orderByKey().equalTo(user_id);
        query.limitToFirst(1);
        query.keepSynced(false);//Todo: this is done to keep the users data only on the server. NOT ON THE PHONE CACHE
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //add new user data only if doesn't exist\\ if first time send to identify phone
                if (!snapshot.exists()) {
                    Log.d(TAG, "onDataChange: snapshot.exists: " + snapshot.exists());
                    user_ref.child(user_id).setValue(topUser).addOnCompleteListener(task -> {
                        //after user is added to real time. add him to fire-store DB
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onDataChange: task success: " + task.isSuccessful());
                            getDocumentForUSer(user_id, email);
                        }

                    }).addOnFailureListener(e -> {
                                closeDial();
                                Log.d(TAG, "onFailure: DB_adding user_error: " + e.getMessage());

                            });


                } else {
                    //Todo: since we decided to do phone verification always to enhance security
                    getDocumentForUSer(user_id, email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                closeDial();
                Log.d(TAG, "onCancelled: auth_error: " + error.getMessage());
            }
        });
    }

    private String provider(String s) {
        return s;
    }

    private void showDialog() {
        alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
        wlp.windowAnimations = R.style.Animation_Design_BottomSheetDialog;
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        alertDialog.getWindow().setAttributes(wlp);
        alertDialog.getWindow().setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.round_edge_rectangle_button_recycler_view));

        View layoutView = mContext.getLayoutInflater().inflate(R.layout.layout_loading_dialog, null);
        alertDialog.setView(layoutView);
        alertDialog.show();
    }

    private void closeDial() {
        alertDialog.dismiss();
    }

    @NonNull
    @Override
    public Task<AuthResult> signInWithEmailAndPassword(@NonNull final String email, @NonNull final String pass){
        showDialog();
        return myAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                mCurrentUser = task1.getResult().getUser();
                Task<GetTokenResult> providerTask = getAccessToken(false);
                try {
                    if (providerTask.isSuccessful()) {
                        final String provider = provider(providerTask.getResult().getSignInProvider());
                        Log.d(TAG, "signInWithEmailAndPassword: provider: " + provider);
                        verifyAccount(mCurrentUser, mContext, provider);
                    }

                } catch (Exception e) {
                    closeDial();
                    signOut();
                    Log.d(TAG, "signInWithEmailAndPassword: task_waiting_error: " + e.getMessage());
                }
            }
        }).addOnFailureListener(e -> {
            closeDial();
            signOut();
            Log.d(TAG, "signInWithEmailAndPassword: Auth_Error: " + e.getMessage());
            Toast.makeText(mContext, "Login error; " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    public Task<GetTokenResult> getAccessToken(boolean b) {
        return myAuth.getAccessToken(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                provider(task.getResult().getSignInProvider());
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "getAccessToken: auth_result_error: " + e.getMessage());
        });
    }

    @NonNull
    @Override
    public Task<Void> updateCurrentUser(@NonNull FirebaseUser firebaseUser) {
        return null;
    }

    @Nullable
    @Override
    public Task<AuthResult> getPendingAuthResult() {
        return null;
    }

    @NonNull
    @Override
    public FirebaseAuthSettings getFirebaseAuthSettings() {
        return null;
    }

    private void sendEmailVer(@NonNull FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "sendEmailVer: task is success: " + task.isSuccessful());
                Toast.makeText(mContext, "We sent you an email verification, check your inbox and click on th link to verify", Toast.LENGTH_SHORT).show();
                closeDial();
                new Handler().postDelayed(() -> {
                    mTraveler.gotoWithFlags(mContext, LoginActivity.class);
                    signOut();
                }, Toast.LENGTH_SHORT);
            }

        }).addOnFailureListener(e -> {
            closeDial();
            Toast.makeText(mContext, "Email not sent! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "verifyUser: Auth_errror: " + e.getMessage());
            signOut();

        });
    }

    @Override
    public void addIdTokenListener(@NonNull IdTokenListener idTokenListener) {
        myAuth.addIdTokenListener(idTokenListener);
    }

    @Override
    public void removeIdTokenListener(@NonNull IdTokenListener idTokenListener) {
        myAuth.removeIdTokenListener(idTokenListener);
    }

    @NonNull
    @Override
    //we return null since we dont want the user to use it. ONLY EMAIL IS ALLOWED
    public Task<AuthResult> signInWithCredential(@NonNull AuthCredential authCredential) {
        return null;
    }

    @NonNull
    @Override
    //we return null since we dont want the user to use it. ONLY EMAIL IS ALLOWED
    public Task<AuthResult> signInWithCustomToken(@NonNull String s) {
        return null;
    }

    @NonNull
    @Override
    //we return null since we dont want the user to use it. ONLY EMAIL IS ALLOWED
    public Task<AuthResult> signInWithEmailLink(@NonNull String s, @NonNull String s1) {
        return null;
    }

    @NonNull
    @Override
    public Task<Void> sendPasswordResetEmail(@NonNull String s) {
        return myAuth.sendPasswordResetEmail(s).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(mContext, "Request successful. Check your inbox ", Toast.LENGTH_SHORT).show();
                mTraveler.gotoWithFlags(mContext, LoginActivity.class);
                // TODO: 10/30/20 fill this here
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sendPasswordResetEmail: auth_error: " + e.getMessage());
        });
    }

    @NonNull
    @Override
    //we return null since we dont want the user to use it. ONLY EMAIL IS ALLOWED
    public Task<Void> sendPasswordResetEmail(@NonNull String s, @Nullable ActionCodeSettings actionCodeSettings) {
        return null;
    }

    @NonNull
    @Override
    //we return null since we dont want the user to use it. ONLY EMAIL IS ALLOWED
    public Task<Void> sendSignInLinkToEmail(@NonNull String s, @NonNull ActionCodeSettings actionCodeSettings) {
        return null;
    }

    @NonNull
    @Override
    //we return null since we dont want the user to use it. ONLY EMAIL IS ALLOWED
    public Task<AuthResult> startActivityForSignInWithProvider(@NonNull Activity activity, @NonNull FederatedAuthProvider federatedAuthProvider) {
        return null;
    }

    @NonNull
    @Override
    //we return null since we dont want the user to use it. ONLY EMAIL IS ALLOWED
    public Task<Void> setFirebaseUIVersion(@Nullable String s) {
        return null;
    }


    @Nullable
    @Override
    public FirebaseUser getCurrentUser() {
        mCurrentUser = mAuth().getCurrentUser();
        Log.d(TAG, "getCurrentUser: mCurrentUser: " + mCurrentUser);
        return mCurrentUser;
    }


    @NonNull
    @Override
    //we return null since we dont want the user to use it. ONLY EMAIL IS ALLOWED
    public Task<AuthResult> signInAnonymously() {
        return null;
    }


    @Override
    public void addIdTokenListener(@NonNull com.google.firebase.auth.internal.IdTokenListener idTokenListener) {
        myAuth.addIdTokenListener(idTokenListener);
    }

    @Override
    public void removeIdTokenListener(@NonNull com.google.firebase.auth.internal.IdTokenListener idTokenListener) {
        myAuth.removeIdTokenListener(idTokenListener);
    }

    @Override
    public void addAuthStateListener(@NonNull AuthStateListener authStateListener) {
        myAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void removeAuthStateListener(@NonNull AuthStateListener authStateListener) {
        myAuth.removeAuthStateListener(authStateListener);
    }

    @NonNull
    @Override
    public Task<AuthResult> createUserWithEmailAndPassword(@NonNull String email, @NonNull String pass) {
        showDialog();
        Task<AuthResult> task = myAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                FirebaseUser user = task1.getResult().getUser();
                sendEmailVer(user);
            }

        }).addOnFailureListener(e -> {
            closeDial();
            Toast.makeText(mContext, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            signOut();
            Log.d(TAG, "createUserWithEmailAndPassword: Auth_error: " + e.getMessage());
        });
        return task;
    }

    @NonNull
    @Override
    public Task<String> verifyPasswordResetCode(@NonNull String s) {
        return myAuth.verifyPasswordResetCode(s).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "verifyPasswordResetCode: task.result: " + task.getResult());
            }
            // TODO: 10/30/20 finish this
        }).addOnFailureListener(e -> {
            Log.d(TAG, "verifyPasswordResetCode: auth_error: " + e.getMessage());
        });
    }
}