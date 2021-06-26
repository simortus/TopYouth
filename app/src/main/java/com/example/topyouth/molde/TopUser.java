package com.example.topyouth.molde;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import java.io.Serializable;

public class TopUser implements  Serializable {

    private String userId, email,  username ,photo, about;

    public TopUser(@NonNull String userId, @NonNull String email, @NonNull String username, @NonNull String photo, @NonNull String about) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.photo = photo;
        this.about = about;
    }

    public TopUser() {}

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoto() {
        return photo;
    }

    public String getAbout() {
        return about;
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
