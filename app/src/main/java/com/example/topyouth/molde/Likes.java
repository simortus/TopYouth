package com.example.topyouth.molde;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Likes implements Serializable {
    private static final String TAG = "Likes";
    private  String userId ;

    public Likes(@NonNull String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public Likes() {
    }
}
