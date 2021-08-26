package com.example.topyouth.auth_database;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class DBSingleton {
    private static final String TAG = "DbSingleton";

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    public FirebaseFirestore getmFirestore() {
        return this.mFirestore;
    }

    public FirebaseStorage getStorage() {
        return this.storage;
    }

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final DatabaseReference users_ref = mDatabase.getReference("users"),
                                    postsRef = mDatabase.getReference("posts"),
                                    commentsRef = mDatabase.getReference("comments"),
                                    likesRef = mDatabase.getReference("likes"),
                                    commentsReactionsRef = mDatabase.getReference("comment_reactions");


    private DBSingleton() {
    }

    public static DBSingleton getInstance() {
        // TODO: 8/13/21  add activity as parameter of the method. check if activity is within the list of the app activities
        //  then return singelton else return null

//       todo>>>  @NonNull Activity activity
//        if (activity.getClass().getName())
        return new DBSingleton();
    }

    public FirebaseDatabase getDbInstance() {
        return this.mDatabase;
    }

    public DatabaseReference getPostsRef() {
        return this.postsRef;
    }

    public DatabaseReference getCommentsRef() {
        return commentsRef;
    }

    public DatabaseReference getLikesRef() {
        return this.likesRef;
    }

    public DatabaseReference getCommentsReactionsRef() {
        return this.commentsReactionsRef;
    }

    public DatabaseReference getUsers_ref() {
        users_ref.keepSynced(false);
        return this.users_ref;
    }

}