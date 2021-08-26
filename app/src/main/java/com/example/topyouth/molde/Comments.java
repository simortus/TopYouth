package com.example.topyouth.molde;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Comments implements Serializable {
    private static final String TAG = "Comments";
    private String postId, user_id, comment_id, commentText, commentDate;

    public Comments(@NonNull String postId, @NonNull String user_id,
                    @NonNull String comment_id ,@NonNull String commentText,@NonNull String commentDate) {
        this.postId = postId;
        this.user_id = user_id;
        this.comment_id = comment_id;
        this.commentText = commentText;
        this.commentDate = commentDate;
    }

    public Comments() {}

    public String getPostId() {
        return postId;
    }

    public String getComment_id() {
        return this.comment_id;
    }

    public String getCommentText() {
        return this.commentText;
    }

    public String getCommentDate() {
        return this.commentDate;
    }

    public String getUser_id() {
        return this.user_id;
    }

}
