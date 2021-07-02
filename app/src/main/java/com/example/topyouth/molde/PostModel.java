package com.example.topyouth.molde;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public final class PostModel implements Serializable {
    private static final String TAG = "PostModel";
    private String postOwnerID;
    private String postId;
    private String imageUrl;
    private String postDetails;
    private ArrayList<TopUser> postLiker, postCommentators;

    public PostModel(String postId, String imageUrl, String postDetails, ArrayList<TopUser> postLiker,
                     ArrayList<TopUser> postCommentators, String postOwnerID) {
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.postDetails = postDetails;
        this.postLiker = postLiker;
        this.postCommentators = postCommentators;
        this.postOwnerID = postOwnerID;
        this.postOwnerID = postOwnerID;
    }

    public String getPostOwnerID() {
        return this.postOwnerID;

    }



    public String getPostId() {
        return this.postId;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public String getPostDetails() {
        return this.postDetails;
    }

    public PostModel(@NonNull final String postId, @NonNull final String imageUrl,
                     @NonNull final String postDetails, @NonNull final String postOwnerID) {
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.postDetails = postDetails;
        this.postOwnerID = postOwnerID;
    }

    public PostModel() {
    }
}
