package com.example.topyouth.molde;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class PostModel implements Serializable {
    private static final String TAG = "PostModel";
    private String postOwnerID;
    private String postId;
    private String imageUrl;
    private String postDetails;
    private ArrayList<TopUser> postLiker, postCommentators;
    private List<Comments> commentList ;
    private List<Likes> likesList;

    public PostModel(@NonNull String postId , @NonNull String postOwnerID, @NonNull String imageUrl,
                     @NonNull String postDetails,@NonNull List<Comments> commentList,@NonNull List<Likes> likesList) {
        this.postId = postId;
        this.postOwnerID = postOwnerID;
        this.imageUrl = imageUrl;
        this.postDetails = postDetails;
        this.commentList = commentList;
        this.likesList = likesList;
    }

    public PostModel(@NonNull String postId, @NonNull String imageUrl,@NonNull String postDetails,
                     @NonNull ArrayList<TopUser> postLiker, @NonNull ArrayList<TopUser> postCommentators,@NonNull String postOwnerID) {
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


    public @NonNull String getPostId() {
        return this.postId;
    }

    public @NonNull String getImageUrl() {
        return this.imageUrl;
    }

    public @NonNull String getPostDetails() {
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
