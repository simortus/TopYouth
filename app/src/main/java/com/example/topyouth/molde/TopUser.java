package com.example.topyouth.molde;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import java.io.Serializable;

public class TopUser implements  Serializable {

    private String userId, email,  username ,photo, about, profession;

    public TopUser(@NonNull String userId, @NonNull String email, @NonNull String username,
                   @NonNull String photo, @NonNull String about, @NonNull String profession) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.photo = photo;
        this.about = about;
        this.profession = profession;
    }

    public TopUser(@NonNull String userId, @NonNull String email, @NonNull String username, @NonNull String photo, @NonNull String about) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.photo = photo;
        this.about = about;
    }

    public TopUser() {}

    @Exclude
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Exclude
    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public void setUsername(String username) {
        this.username = username;
    }

    @Exclude
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Exclude
    public void setAbout(String about) {
        this.about = about;
    }

    @Exclude
    public void setProfession(String profession) {
        this.profession = profession;
    }

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

    public String getProfession() {
        return profession;
    }

    @NonNull
    @Override
    public String toString() {
        return "user_id: "+getUserId()+"\n"+
                "email: "+getEmail()+"\n"+
                "username: "+getUsername()+"\n"+
                "photo: "+getPhoto()+"\n"+
                "profession: "+getProfession()+"\n"+
                "about: "+getAbout()+"\n";
    }
}