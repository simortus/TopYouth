package com.example.topyouth.molde;

import androidx.annotation.NonNull;

public class CommentReaction {
    private static final String TAG = "CommentReaction";

    private String postId;
    private String user_id;
    private String comment_id;
    private String reaction;

    public CommentReaction(@NonNull String postId, @NonNull String user_id,
                           @NonNull  String comment_id,@NonNull String reaction) {
        this.postId = postId;
        this.user_id = user_id;
        this.comment_id = comment_id;
        this.reaction = reaction;
    }

    public String getPostId() {
        return postId;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getComment_id() {
        return comment_id;
    }

    public CommentReaction() {}

    public String getReaction() {
        return reaction;
    }
}
