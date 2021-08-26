package com.example.topyouth.molde;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import java.io.Serializable;

public class TopUser implements  Serializable {
    private static final String TAG = "TopUser";
    private String userId, email,  username ,photo, about;

    public TopUser(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public TopUser(@NonNull String userId, @NonNull String email, @NonNull String username, @NonNull String photo, @NonNull String about) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.photo = photo;
        this.about = about;
    }

    public TopUser() {}

    public TopUser(@NonNull String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPhoto() {
        return this.photo;
    }

    public String getAbout() {
        return this.about;
    }

    @NonNull
    @Override
    public String toString() {
        return "user_id: "+getUserId()+"\n"+
                "email: "+getEmail()+"\n"+
                "username: "+getUsername()+"\n"+
                "photo: "+getPhoto()+"\n"+
                "about: "+getAbout()+"\n";
    }
}
