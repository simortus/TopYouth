package com.example.topyouth.utility_classes;


import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class DBSingelton {

    private static final String TAG = "DbSingleton";

    private  final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private final FirebaseFirestore mFirestore  = FirebaseFirestore.getInstance();

    public FirebaseFirestore getmFirestore() {
        return this.mFirestore;
    }

    public  FirebaseStorage getStorage() {
        return this.storage;
    }

    private  final FirebaseStorage storage = FirebaseStorage.getInstance();
    private  final DatabaseReference users_ref = mDatabase.getReference("users"),
            postsRef = mDatabase.getReference("posts"),
            commentsRef = mDatabase.getReference("comments"),
            likesRef = mDatabase.getReference("likes");


    private DBSingelton() {
    }

    public static DBSingelton getInstance() {
        return new DBSingelton();
    }

    public FirebaseDatabase getDbInstance() {
        return mDatabase;
    }

    public  DatabaseReference getPostsRef() {
        return postsRef;
    }

    public  DatabaseReference getCommentsRef() {
        return commentsRef;
    }

    public  DatabaseReference getLikesRef() {
        return likesRef;
    }

    public DatabaseReference getUsers_ref() {
        users_ref.keepSynced(false);
        return users_ref;
    }

}