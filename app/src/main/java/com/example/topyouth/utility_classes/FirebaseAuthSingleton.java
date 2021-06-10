package com.example.topyouth.utility_classes;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.topyouth.home.MainActivity;
import com.example.topyouth.login.LoginActivity;
import com.example.topyouth.molde.TopUser;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author mohamed-msaad
 * Auth class used to override the Firebase auth methods and return only the needed functionalities
 **/
public class FirebaseAuthSingleton extends FirebaseAuth {
    private static final String TAG = "AuthSingleton";
    private Context mContext;
    //<<----- auth, DB---->>
    private final static FirebaseAuth myAuth = FirebaseAuth.getInstance();
    private static FirebaseUser mCurrentUser;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference users_ref = database.getReference("users");
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private DBSingelton mDbSingleton = DBSingelton.getInstance();


    //vars
    private final Traveler mTraveler = new Traveler();

    //<<------methods--------->>
    private FirebaseAuthSingleton(@NonNull Context context) {
        super(FirebaseApp.getInstance());
        this.mContext = context;
    }

    //singleton of this class
    public static FirebaseAuthSingleton getInst(@NonNull Context context) {
        if (myAuth != null) {
            return new FirebaseAuthSingleton(context);
        } else
            return null;
    }

    public FirebaseAuth mAuth() {
        return myAuth;
    }

    public boolean isUserCompliant(FirebaseUser currentUser) {
        return currentUser != null && currentUser.isEmailVerified() && !currentUser.isAnonymous();
    }

    @Override
    public void signOut() {
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
                signOut();//todo: Mo's notes: security stuff: close auth and databases connections when things are canceled or failed
//                    mDatabase.goOffline();//todo: Mo's notes: security stuff: close auth and databases connections when things are canceled or failed
                Toast.makeText(context, "Please verify your account.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d(TAG, "verifyAccount: error: " + e.getMessage());
            Toast.makeText(context, "Something went wrong..." + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            signOut();
        }
    }

    //works like a charm
    //check if user exists in fire-store and adds it if not else go to main
    private void getDocumentForUSer(@NonNull final String id, @NonNull final String email) {
        final Map<String, Object> map = new HashMap<>();
        map.put("user_id", id);
        map.put("status", "not_approved");
        DocumentReference thisUserDoc = mFirestore.collection("app_us").document(id);
        thisUserDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                createDocument(id, email);
            }
            if (documentSnapshot.exists()) {
                Log.d(TAG, "getDocumentForUSer: documentSnapshot id: " + documentSnapshot.getId());
                //todo here we check if the shared pref is true for phone verified
                mTraveler.gotoWithFlags(mContext, MainActivity.class);
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: not worked: " + e.getMessage());
        });

    }

//    private void isAdminApproved() {
//        final String user_id = getCurrentUser().getUid();
////        AtomicBoolean is = new AtomicBoolean(false);
//
//        DocumentReference not_approved_users = mFirestore.collection("app_us").document(user_id);
//        not_approved_users.addSnapshotListener((value, error) -> {
//            if (error == null) {
//                Log.d(TAG, "onEvent: Document value_id: " + value.getId());
//                Log.d(TAG, "onEvent: Document value_approved_status: " + value.get("status"));
//                final String status = (String) value.get("status");
//                Log.d(TAG, "isAdminApproved: status: " + status);
//                boolean iss = status.equalsIgnoreCase("approved");
//                setApproved(iss);
//                if (iss) {
//                    isApproved = true;
//                    setApproved(iss);
//                }
//
//
//                Log.d(TAG, "isAdminApproved: " + iss);
//            }
//        });
//
//    }

    //works like a charm
    private void createDocument(@NonNull final String id, @NonNull final String email) {
        final Map<String, Object> map = new HashMap<>();
        map.put("user_id", id);
        map.put("email", email);
        map.put("status", "not_approved");
        mFirestore.collection("app_us").document(id).set(map).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "onSuccess: Successful operation: " + documentReference);
            mTraveler.gotoWithFlags(mContext, MainActivity.class);

        }).addOnFailureListener(e ->
                Log.d(TAG, "onFailure: not worked: " + e.getMessage()));
    }

    // //  works like a charm
    private void addNewUser(@NonNull final String email, @NonNull final String user_id) {
        final String name = "no_name",
                phone = "no_phone",
                profileUrl = "no_url";
        //todo Mo's: this is to prevent malicious users from trying to skip the phone confirmation, by trying to add a phone number at this step
        final TopUser topUser = new TopUser(user_id, name, email, phone, profileUrl);
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

                    }).addOnFailureListener(e ->
                            Log.d(TAG, "onFailure: DB_adding user_error: " + e.getMessage()));

                } else {
                    //Todo: since we decided to do phone verification always to enhance security
                    getDocumentForUSer(user_id, email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: auth_error: " + error.getMessage());
            }
        });
    }

    private String provider(String s) {
        final String ss = s;
        return ss;
    }

    @NonNull
    @Override
    public Task<AuthResult> signInWithEmailAndPassword(@NonNull final String email, @NonNull final String pass) {
        return myAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                mCurrentUser = task1.getResult().getUser();
                Task<GetTokenResult> providerTask = getAccessToken(false);
                try {
                    if (providerTask.isSuccessful()) {
                        final String provider = provider(providerTask.getResult().getSignInProvider());
                        Log.d(TAG, "signInWithEmailAndPassword: provider: " + provider);
                        verifyAccount(mCurrentUser, mContext, provider);
//                        if (isUserCompliant(mCurrentUser) && p.equals("password")) {
//                            // TODO: 6/3/21 need to go to home if all is ok
//                            // TODO: Add user to database and firestore if not exist, else go to home.
//                            Toast.makeText(mContext, "Works fine!", Toast.LENGTH_SHORT).show();
//                            @NonNull final String user_id = mCurrentUser.getUid();
//                            addNewUser(email, user_id);
//
//
//                        } else {
//                            Toast.makeText(mContext, "Please verify your account!", Toast.LENGTH_SHORT).show();
//                            signOut();
//                        }
                    }

                } catch (Exception e) {
                    signOut();
                    Log.d(TAG, "signInWithEmailAndPassword: task_waiting_error: " + e.getMessage());
                }
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "signInWithEmailAndPassword: Auth_Error: " + e.getMessage());
            Toast.makeText(mContext, "Login error; " + e.getMessage(), Toast.LENGTH_SHORT).show();
            signOut();
        });
    }

    @NonNull
    @Override
    public Task<GetTokenResult> getAccessToken(boolean b) {
        Task<GetTokenResult> tokenResultTask = myAuth.getAccessToken(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                provider(task.getResult().getSignInProvider());
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "getAccessToken: auth_result_error: " + e.getMessage());
        });
        return tokenResultTask;
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
                new Handler().postDelayed(() -> {
                    mTraveler.gotoWithFlags(mContext, LoginActivity.class);
                    signOut();
                }, Toast.LENGTH_SHORT);
            }

        }).addOnFailureListener(e -> {
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
        Task<AuthResult> task = myAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                FirebaseUser user = task1.getResult().getUser();
                sendEmailVer(user);
            }

        }).addOnFailureListener(e -> {
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


//    @SuppressLint("ParcelCreator")
//    public class MyFUSER extends FirebaseUser {
//        private static final String TAG = "MyFUSER";
//        private Context mContext;
//        private FirebaseAuth mAuth;
//        private Traveler mTraveler = new Traveler();
//        private Hashing hash = new Hashing();
//        private FirebaseUser current_F_user;
//
//        public MyFUSER(Context context, FirebaseAuth auth) {
//            mContext = context;
//            mAuth = auth;
//            current_F_user = auth.getCurrentUser();
//
//        }
//
//        public FirebaseUser getCurrent_F_user() {
//            return mAuth.getCurrentUser();
//        }
//
//        @NonNull
//        @Override
//        public Task<Void> reauthenticate(@NonNull AuthCredential authCredential) {
//            boolean isok = current_F_user != null && current_F_user.isEmailVerified() && !current_F_user.isAnonymous();
//            if (isok && authCredential.getProvider().equals("password")) {
//                return current_F_user.reauthenticate(authCredential).addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "reauthenticate: success: " + task.isSuccessful());
//                    }
//                }).addOnFailureListener(e -> {
//                    Log.d(TAG, "reauthenticate: error: " + e.getMessage());
//                });
//            } else
//                return null;
//        }
//
//        @NonNull
//        @Override
//        public Task<AuthResult> reauthenticateAndRetrieveData(@NonNull AuthCredential authCredential) {
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public Task<AuthResult> startActivityForReauthenticateWithProvider(@NonNull Activity activity, @NonNull FederatedAuthProvider federatedAuthProvider) {
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public Task<AuthResult> linkWithCredential(@NonNull AuthCredential authCredential) {
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public Task<AuthResult> startActivityForLinkWithProvider(@NonNull Activity activity, @NonNull FederatedAuthProvider federatedAuthProvider) {
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public Task<AuthResult> unlink(@NonNull String s) {
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public Task<Void> updateProfile(@NonNull UserProfileChangeRequest userProfileChangeRequest) {
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public Task<Void> updateEmail(@NonNull String s) {
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public Task<Void> updatePhoneNumber(@NonNull PhoneAuthCredential phoneAuthCredential) {
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public Task<Void> updatePassword(@NonNull String s) {
//
//            boolean hasDigit = hash.hasDigits(s),
//                    hasSpecChar = hash.hasSpecial(s),
//                    hasNum = hash.hasDigits(s),
//                    longEnough = hash.isLongEnough(s);
//            Task<Void> updatePassTask = current_F_user.updatePassword(s);
//
//            if (longEnough && hasDigit && hasSpecChar && hasNum) {
//
//                return updatePassTask.addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(mContext, "Pass updated Successfully", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(e -> {
//                    Log.d(TAG, "updatePassword: error: " + e.getMessage());
//                    Toast.makeText(mContext, "Pass update error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//            } else
//                return null;
//        }
//
//
//        @NonNull
//        @Override
//        public Task<Void> delete() {
//
//            if (mAuth != null) {
//                Task<Void> deleteUserTask = current_F_user.delete().addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        mAuth.signOut();
//                        mTraveler.goToWithFlags(mContext, LoginActivity.class);
//
//                    }
//                }).addOnFailureListener(e -> {
//                    Log.d(TAG, "delete: error: " + e.getMessage());
//                });
//
//                return deleteUserTask;
//            } else return null;
//        }
//
//        @NonNull
//        @Override
//        public Task<Void> sendEmailVerification() {
//            Context context = mContext.getApplicationContext();
//            if (context != null && mAuth != null) {
//                return current_F_user.sendEmailVerification().addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(context, "We sent you an email verification, check your inbox and click on th link to verify", Toast.LENGTH_SHORT).show();
//                        mAuth.signOut();
//                        Log.d(TAG, "sendEmailVerification: ");
//                    }
//                    mAuth.signOut();
//
//                }).addOnFailureListener(e -> {
//                    Toast.makeText(context, "Email not sent! " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, "verifyUser: Auth_errror: " + e.getMessage());
//                    mAuth.signOut();
//
//                });
//            }
//            return null;
//
//        }
//
//        @NonNull
//        @Override
//        public Task<Void> sendEmailVerification(@NonNull ActionCodeSettings actionCodeSettings) {
//            return super.sendEmailVerification(actionCodeSettings);
//        }
//
//        @NonNull
//        @Override
//        public Task<Void> verifyBeforeUpdateEmail(@NonNull String s) {
//            return super.verifyBeforeUpdateEmail(s);
//        }
//
//        @NonNull
//        @Override
//        public Task<Void> verifyBeforeUpdateEmail(@NonNull String s, @Nullable ActionCodeSettings actionCodeSettings) {
//            return super.verifyBeforeUpdateEmail(s, actionCodeSettings);
//        }
//
//
//        @NonNull
//        @Override
//        public String getUid() {
//
//            return current_F_user.getUid();
//        }
//
//        @NonNull
//        @Override
//        public String getProviderId() {
//            return current_F_user.getProviderId();
//        }
//
//        @Override
//        public boolean isAnonymous() {
//            return current_F_user.isAnonymous();
//        }
//
//        @Nullable
//        @Override
//        public List<String> zza() {
//            return current_F_user.zza();
//        }
//
//        @NonNull
//        @Override
//        public List<? extends UserInfo> getProviderData() {
//
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public FirebaseUser zza(@NonNull List<? extends UserInfo> list) {
//            return null;
//        }
//
//        @Override
//        public FirebaseUser zzb() {
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public FirebaseApp zzc() {
//            return null;
//        }
//
//        @Nullable
//        @Override
//        public String getDisplayName() {
//            return current_F_user.getDisplayName();
//        }
//
//        @Nullable
//        @Override
//        public Uri getPhotoUrl() {
//            return current_F_user.getPhotoUrl();
//        }
//
//        @Nullable
//        @Override
//        public String getEmail() {
//            return current_F_user.getEmail();
//        }
//
//        @Nullable
//        @Override
//        public String getPhoneNumber() {
//            return current_F_user.getPhoneNumber();
//        }
//
//        @Override
//        public boolean isEmailVerified() {
//            return current_F_user.isEmailVerified();
//        }
//
//        @Nullable
//        @Override
//        public String getTenantId() {
////        return mUser.getTenantId();
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public zzff zzd() {
//            return null;
//        }
//
//        @Override
//        public void zza(@NonNull zzff zzff) {
//        }
//
//        @NonNull
//        @Override
//        public String zze() {
//            return null;
//        }
//
//        @NonNull
//        @Override
//        public String zzf() {
//            return null;
//        }
//
//        @Nullable
//        @Override
//        public FirebaseUserMetadata getMetadata() {
//            return current_F_user.getMetadata();
//        }
//
//        @NonNull
//        @Override
//        public MultiFactor getMultiFactor() {
//            return null;
//        }
//
//        @Override
//        public void zzb(List<MultiFactorInfo> list) {
//
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//
//        }
//
//    }

//    public void signIn(@NonNull Activity activity, @NonNull String email, @NonNull String password) {
//        Context context = activity.getApplicationContext();
//        if (!email.isEmpty() && !password.isEmpty()) {
//            myAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
//                        // if successful send email verification
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "login: successful: ");
//                            if (task.getResult().getUser() != null) {
//                                mCurrentUser = task.getResult().getUser();
//                                // Todo> The User object has been made bellow. Pass it to the next activity or fragment. Sina's Note <That is not neccessary. we dont pass through intent but rather fetch it from Db in the other activity>
//                            }
//                        }
//                    }
//            ).addOnFailureListener(e -> {
//                Log.d(TAG, "signIn: Failed: " + e.getMessage());
//                signOut();
//                mDatabase.goOffline();
//                Toast.makeText(context, "Unable to reach the host. Check your Internet " + e.getMessage(), Toast.LENGTH_LONG).show();
//            }).addOnCanceledListener(() -> {
//                Log.d(TAG, "signIn: canceled");
//                signOut();
//                mDatabase.goOffline(); //Todo Mo's modifications. here we close the com.example.prototype1.auth and also the db server connection!
//            });
//        } else {
//            Toast.makeText(context, "Type a valid email and password", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private void checkUserNumber(@NonNull DataSnapshot snapshot, @NonNull Context context) {
//        Traveler mTraveler = new Traveler();
//        for (DataSnapshot ds : snapshot.getChildren()) {
//            User user1 = ds.getValue(User.class);
//            Log.d(TAG, "onDataChange: user1: " + user1.getPhone());
//            if (user1.getPhone().equals("no_phone")) {
//                // TODO since we decided to always identify the phone with a message then always go to PhoneActivity
//
//                mTraveler.goToWithFlags(context, PhoneVerifyActivity.class);
//            }
//        }
//
//    }
}