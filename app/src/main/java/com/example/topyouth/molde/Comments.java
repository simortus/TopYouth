package com.example.topyouth.molde;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Comments implements Serializable {
    private static final String TAG = "Comments";
    private String user_id, comment_id, commentText, commentDate;


    public Comments(@NonNull String user_id,@NonNull String comment_id,@NonNull String commentDate ,@NonNull String commentText) {
        this.user_id = user_id;
        this.comment_id = comment_id;
        this.commentDate = commentDate;
        this.commentText = commentText;
    }

    public Comments() {
    }

    public String getComment_id() {
        return comment_id;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public String getUser_id() {
        return user_id;
    }
}
