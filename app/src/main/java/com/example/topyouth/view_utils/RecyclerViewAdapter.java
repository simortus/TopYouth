package com.example.topyouth.view_utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.topyouth.R;
import com.example.topyouth.molde.CommentReaction;
import com.example.topyouth.molde.Comments;
import com.example.topyouth.molde.Likes;
import com.example.topyouth.molde.PostModel;
import com.example.topyouth.molde.TopUser;
import com.example.topyouth.auth_database.DBSingleton;
import com.example.topyouth.auth_database.FirebaseAuthSingleton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CardViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    //database
    private final Activity mContext;

    private List<PostModel> postsList;
    private List<TopUser> postOwnerList;
    private List<Likes> postLikes;
    private List<Comments> postComments;
    private Drawable like_sign, liked_sign;


    //database
    private FirebaseUser currentUser;
    private FirebaseAuthSingleton singleton;
    private final DBSingleton dbSingleton = DBSingleton.getInstance();
    private final DatabaseReference postRef = dbSingleton.getPostsRef(),
            userRef = dbSingleton.getUsers_ref(),
            commentRef = dbSingleton.getCommentsRef(),
            likeRef = dbSingleton.getLikesRef();
    // threads
    private final ExecutorService executors = Executors.newCachedThreadPool();


    private final List<TopUser> commentators = new ArrayList<>();
    private final List<Comments> commentsList = new ArrayList<>();
    private final List<CommentReaction> reactionList = new ArrayList<>();
    private ListViewAdapter myadapter;
    private PostModel currentPost;


    public RecyclerViewAdapter(@NonNull final Activity mContext, @NonNull List<PostModel> postsList,
                               @NonNull List<TopUser> postOwnerList) {
        this.mContext = mContext;
        this.postsList = postsList;
        this.postOwnerList = postOwnerList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);
        like_sign = mContext.getDrawable(R.drawable.like_sign_outlined);
        liked_sign = mContext.getDrawable(R.drawable.liked_icone);

        return new CardViewHolder(view);
    }


    /**
     * Method that inflates a single item view by inflating all the widgets
     * from a given item
     */
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        singleton = FirebaseAuthSingleton.getInst(mContext);
        currentUser = singleton.mAuth().getCurrentUser();
        if (!postsList.isEmpty()) {
            currentPost = postsList.get(position);
            if (!postOwnerList.isEmpty()) {
                TopUser user = postOwnerList.get(position);
                String userName = user.getUsername(),
                        userProfile = user.getPhoto();
                holder.postUsername.setText(userName);
                if (!userProfile.equals("no_photo")) {
                    // post owner data
                    Glide.with(mContext).load(userProfile).fitCenter().into(holder.postUserPorfileImage);
                } else {
                    holder.postUserPorfileImage.setBackground(mContext.getDrawable(R.drawable.profile));
                }
            }

            //post details
            final String url = currentPost.getImageUrl();
            Log.d(TAG, "onBindViewHolder: post_url: " + url);
            holder.postDetails.setText(currentPost.getPostDetails());
            Glide.with(this.mContext).load(url).centerCrop().into(holder.postImageView);
            setupLikesAndComments(currentPost, holder);
        }


    }

    private void setupLikesAndComments(final @NonNull PostModel currentPost, @NonNull CardViewHolder holder) {
        final String postId = currentPost.getPostId();
        final String userid = currentUser.getUid();
        final Query likeQuer = likeRef.child(postId);
        final Query commQuer = commentRef.child(postId);

        //1st setup the like button if current user liked it
        //2nd setup lik numbers
        //3rd setup comment numbers
        Runnable likeRun = () -> likeQuer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child(userid).exists()) {
                        holder.likeImageButton.setImageDrawable(mContext.getDrawable(R.drawable.liked_icone));
                        holder.likeText.setText(R.string.unlike);
                    } else {
                        holder.likeImageButton.setImageDrawable(mContext.getDrawable(R.drawable.like_sign_outlined));
                        holder.likeText.setText(R.string.like);
                    }
                }
                if (dataSnapshot.hasChildren()) {
                    final String numberofLikes = String.valueOf(dataSnapshot.getChildrenCount());
                    int likeNumbers = Integer.valueOf(numberofLikes);
                    if (likeNumbers == 1) {
                        holder.postLikeNumber.setText(numberofLikes + " like");
                    }
                    if (likeNumbers > 1) {
                        holder.postLikeNumber.setText(numberofLikes + " likes");
                    }

                } else {
                    holder.postLikeNumber.setText(null);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Error: " + databaseError.getMessage());
            }
        });

        Runnable commentRun = () -> commQuer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int counter = 0;
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: postNode_child_id: " + ds.getKey());
                        for (DataSnapshot snapshot : ds.getChildren()) {
                            Log.d(TAG, "onDataChange: user_id children: " + snapshot.getKey());
                            counter++;
                        }
                    }
                }
                if (counter == 0) {
                    holder.postCommentNumber.setText(null);
                } else if (counter == 1) {
                    holder.postCommentNumber.setText(counter + " comment");
                } else {
                    holder.postCommentNumber.setText(counter + " comments");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Error: " + databaseError.getMessage());
            }
        });
        executors.execute(likeRun);
        executors.execute(commentRun);
    }


    @Override
    public int getItemCount() {
        return postsList.size();
    }

    class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final String TAG = "CardViewHolder";

        //view
        private CircularImageView postUserPorfileImage;
        private TextView postUsername, postDetails, postLikeNumber, postCommentNumber, likeText;
        private ImageButton settingImageButton, likeImageButton, commentImageButton;
        private ImageView postImageView;


        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            setupView(itemView);
        }

        private void setupView(View itemView) {
            postUserPorfileImage = itemView.findViewById(R.id.post_user_image);
            postDetails = itemView.findViewById(R.id.post_details);
            postUsername = itemView.findViewById(R.id.post_user_name);
            postLikeNumber = itemView.findViewById(R.id.post_likes_number);
            postCommentNumber = itemView.findViewById(R.id.post_comments_number);
            settingImageButton = itemView.findViewById(R.id.post_setting);
            likeImageButton = itemView.findViewById(R.id.like_button);
            commentImageButton = itemView.findViewById(R.id.comment_button);
            postImageView = itemView.findViewById(R.id.post_image);
            likeText = itemView.findViewById(R.id.like_text);

            //click listeners handlers
            likeImageButton.setOnClickListener(this);
            settingImageButton.setOnClickListener(this);
            commentImageButton.setOnClickListener(this);
            postCommentNumber.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.post_setting:
                    Log.d(TAG, "onClick: post_setting clicked");
                    break;

                case R.id.like_button:
                    createLike();
                    break;

                case R.id.comment_button:
                    displayCommentLayoutDialog();
                    break;
                case R.id.post_comments_number:
                    openCommentDisplay();
                    break;
            }
        }


        private void openCommentDisplay() {
            final View layoutView = mContext.getLayoutInflater().inflate(R.layout.comment_display_layout, null);
            final ListView listView = layoutView.findViewById(R.id.list_view);
            myadapter = new ListViewAdapter(mContext, commentsList, commentators, reactionList);
            listView.setAdapter(myadapter);


            //Dialog constructor and setup
            final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            final AlertDialog alertDialog = dialog.create();
            alertDialog.setCanceledOnTouchOutside(false);

            final WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
            wlp.windowAnimations = R.style.Widget_AppCompat_Light_ListPopupWindow;
            wlp.gravity = Gravity.CENTER;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            alertDialog.getWindow().setAttributes(wlp);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            dialog.setNegativeButton("Done", (dialogInterface, i) -> dialogInterface.dismiss());

            dialog.setView(layoutView);

            //Data  and layout setup
            final String postID = postsList.get(getAdapterPosition()).getPostId();
            final Query commentQuery = commentRef.child(postID);
            commentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    commentsList.clear();
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChildren()) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                final String userId = ds.getKey();
                                Log.d(TAG, "onDataChange: userId: " + userId);
                                fetchUser(userId);
                                for (DataSnapshot dss : ds.getChildren()) {
                                    final Comments comment = dss.getValue(Comments.class);
                                    commentsList.add(comment);
                                    getCommentReaction(comment.getComment_id());
                                    Log.d(TAG, "onDataChange: commentSnapshot id: " + comment.getComment_id());
                                    Log.d(TAG, "onDataChange: commentSnapshot comment text: " + comment.getCommentText());
                                }
                            }
                            myadapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: databaseError: " + databaseError.getMessage());
                    alertDialog.dismiss();
                }
            });
//            getCommentReaction(postID);
            dialog.show();

        }

        private void getCommentReaction(@NonNull String commentId) {
//             final Query query = commentsReactionsRef.child(commentId).orderByKey().equalTo(userId);
            //need commentID
            //userId
            final Runnable getCommentReactionRunnable = () -> {
                final DatabaseReference commentLikeRef = dbSingleton.getCommentsReactionsRef();
                final Query commentReactionQuery = commentLikeRef.child(commentId).orderByKey();

                commentReactionQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        reactionList.clear();
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChildren()) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "getCommentReaction onDataChange: ds.Key: " + ds.getKey());
                                    CommentReaction reaction = ds.getValue(CommentReaction.class);
                                    Log.d(TAG, "getCommentReaction onDataChange: ds.Key: " + reaction.getComment_id());
                                    Log.d(TAG, "getCommentReaction onDataChange: ds.Key: " + reaction.getReaction());
                                    reactionList.add(reaction);
//                                    for (DataSnapshot dss : ds.getChildren()) {
//                                        CommentReaction reaction = dss.getValue(CommentReaction.class);
//                                        Log.d(TAG, "getCommentReaction onDataChange: ds.Key: " + reaction.getComment_id());
//                                        Log.d(TAG, "getCommentReaction onDataChange: ds.Key: " + reaction.getReaction());
//                                        reactionList.add(reaction);
//                                    }
                                }
                                myadapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "getCommentReaction onDataChange: ds.Key: " + dataSnapshot.exists());
                            }
//                        Log.d(TAG, "onDataChange: comment_id: " + dataSnapshot.getKey());
//                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                            final CommentReaction reaction = ds.getValue(CommentReaction.class);
//                            Log.d(TAG, "getCommentReaction onDataChange: comment_id: " + reaction.getComment_id());
//                            Log.d(TAG, "getCommentReaction onDataChange: comment Reaction: " + reaction.getReaction());
//                            reactionList.add(reaction);
//                        }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "getCommentReaction onCancelled: databaseError: " + databaseError.getMessage());
                    }
                });
            };
            executors.execute(getCommentReactionRunnable);
        }

        private void fetchUser(final String userId) {
            final Query userQuery = userRef.child(userId);
            userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        commentators.clear();
                        final TopUser currentuser = dataSnapshot.getValue(TopUser.class);
                        commentators.add(currentuser);
                        myadapter.notifyDataSetChanged();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: databaseError: " + databaseError.getMessage());

                }
            });
        }

        //works like a charm
        private void displayCommentLayoutDialog() {
            final View layoutView = mContext.getLayoutInflater().inflate(R.layout.comment_layout, null);
            final Button sendButton = layoutView.findViewById(R.id.commentLayout_sendButton);
            final Button cancelButton = layoutView.findViewById(R.id.commentLayout_cancelButton);
            final EditText commentEditText = layoutView.findViewById(R.id.commentLayout_edit_text);
            final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setView(layoutView);

            final AlertDialog alertDialog = dialog.create();
            WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
            wlp.windowAnimations = R.style.MaterialAlertDialog_MaterialComponents_Title_Panel;
            wlp.gravity = Gravity.CENTER;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            alertDialog.getWindow().setAttributes(wlp);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.getWindow().setBackgroundDrawable(mContext.getDrawable(R.drawable.round_edge_rectangle_button_recycler_view));
            alertDialog.show();

            sendButton.setOnClickListener(view1 -> {
                Log.d(TAG, "onClick: sendButton clicked");
                final String comment = commentEditText.getText().toString();
                if (!comment.isEmpty()) {
                    createComment(comment);
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(mContext, "Please type something", Toast.LENGTH_SHORT).show();
                }
            });


            cancelButton.setOnClickListener(view12 -> {
                Log.d(TAG, "onClick: cancelButton clicked");
                alertDialog.dismiss();
            });


        }

        //works like a charm
        private void createLike() {
            final String user_id = currentUser.getUid();
            final String postID = postsList.get(getAdapterPosition()).getPostId();
            Log.d(TAG, "createLike: postID: " + postID);

            Runnable likeRun = () -> {
                final Likes like = new Likes(user_id);
                final Query likeQuery = likeRef.child(postID).child(user_id);
                likeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            likeRef.child(postID).child(user_id).setValue(like).addOnSuccessListener(aVoid -> {
                                likeImageButton.setImageDrawable(mContext.getDrawable(R.drawable.liked_icone));
                                likeText.setText(R.string.unlike);
                                Log.d(TAG, "onSuccess: like added successfully");
                                Toast.makeText(mContext, "like added", Toast.LENGTH_SHORT).show();


                            }).addOnFailureListener(e -> {
                                Log.d(TAG, "onFailure: failed adding like: " + e.getLocalizedMessage());
                                Toast.makeText(mContext, "like failed", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            likeRef.child(postID).child(user_id).removeValue().addOnSuccessListener(aVoid -> {
                                likeImageButton.setImageDrawable(mContext.getDrawable(R.drawable.like_sign_outlined));
                                likeText.setText(R.string.like);

                                Log.d(TAG, "onSuccess: like removed successfully");
                                Toast.makeText(mContext, "Like removed", Toast.LENGTH_SHORT).show();

                            }).addOnFailureListener(e -> {
                                Log.d(TAG, "onFailure: failed removing like: " + e.getLocalizedMessage());
                                Toast.makeText(mContext, "Unlike failed", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: like error: " + databaseError.getMessage());
                    }
                });

            };

            executors.execute(likeRun);
        }

        //works like a charm
        private void createComment(@NonNull final String commentText) {
            final String user_id = currentUser.getUid();
            final String postID = postsList.get(getAdapterPosition()).getPostId();
            Log.d(TAG, "createComment: postID: " + postID);
            final String comment_id = UUID.randomUUID().toString();
            final String commentDate = new Date(System.currentTimeMillis()).toString();
            Log.d(TAG, "createComment: comment date: " + commentDate);

            final Runnable commentRun = () -> {
                final Comments comment = new Comments(postID, user_id, comment_id, commentText, commentDate);
                commentRef.child(postID).child(user_id).child(comment_id).setValue(comment).addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onSuccess: comment added successfully");
                    Toast.makeText(mContext, "Comment added", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: failed adding comment: " + e.getLocalizedMessage());
                    Toast.makeText(mContext, "Comment failed", Toast.LENGTH_SHORT).show();
                });
            };

            executors.execute(commentRun);

        }


    }


    private class ListViewAdapter extends ArrayAdapter<Comments> {
        private static final String TAG = "ListViewAdapter";

        //constructor parameters
        private final List<Comments> commentList;
        private final List<TopUser> commnters;
        private final Context mContext;

        //view vars
        private ImageButton likeButton, loveButton, dislikeButton;
        private CircularImageView commenterProfile;
        private TextView usernameTextView, commentTextView, commentDateTextView;
        private Drawable liked, loved, disliked, like, love, dislike;

        // setup vars
        private List<CommentReaction> commentReactionList = new ArrayList<>();
        private final String[] labels = {"like", "love", "dislike"};

        private Comments currentComment;

        @SuppressLint("ResourceType")
        public ListViewAdapter(@NonNull final Context context, final List<Comments> commentList,
                               final List<TopUser> commenters, final List<CommentReaction> reactionList) {
            super(context, R.layout.comment_display_layout, commentList);
            this.mContext = context;
            this.commentList = commentList;
            this.commnters = commenters;
            this.commentReactionList = reactionList;

        }

        @Nullable
        @Override
        public Comments getItem(int position) {
            return commentList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            final View viewHolder = inflater.inflate(R.layout.comment_layout_list, parent, false);
            // setup view
            setupView(viewHolder);


            if (commentList.size() > 0) {
                // objects
                currentComment = commentList.get(position);
                Log.d(TAG, "getView: current comment: " + currentComment);
                //setup comment details
                final String comment = currentComment.getCommentText();
                final String comment_date = currentComment.getCommentDate();

                if (comment != null) {
                    commentTextView.setText(comment);
                    commentDateTextView.setText(comment_date);
                }

                for (TopUser commenter : commnters) {
                    if (commenter.getUserId().equals(currentComment.getUser_id())) {
                        Glide.with(mContext).load(commenter.getPhoto()).centerCrop().into(commenterProfile);
                        usernameTextView.setText(commenter.getUsername());
                    }
                }


            }

            if (!commentReactionList.isEmpty()) {
                for (CommentReaction cr : commentReactionList) {
                    if (cr.getComment_id().equals(currentComment.getComment_id())) {
                        if (currentUser.getUid().equals(cr.getUser_id())) {
                            final String reaction = cr.getReaction();
                            switch (reaction) {
                                case "like":
                                    likeButton.setImageDrawable(liked);
                                    break;
                                case "love":
                                    loveButton.setImageDrawable(loved);
                                    break;
                                case "dislike":
                                    dislikeButton.setImageDrawable(disliked);
                                    break;
                            }
                        }
                    }
                }
            }


            likeButton.setOnClickListener(v -> {
                likeCurrentComment(currentComment, labels[0]);
            });
            loveButton.setOnClickListener(v -> {
                likeCurrentComment(currentComment, labels[1]);
            });
            dislikeButton.setOnClickListener(v -> {
                likeCurrentComment(currentComment, labels[2]);
            });
            return viewHolder;
        }

        private void setupView(@NonNull View viewHolder) {
            commenterProfile = viewHolder.findViewById(R.id.commenter_profilePic);
            usernameTextView = viewHolder.findViewById(R.id.username);
            commentTextView = viewHolder.findViewById(R.id.comment_textView);
            commentDateTextView = viewHolder.findViewById(R.id.comment_date);

            likeButton = viewHolder.findViewById(R.id.like_button);
            loveButton = viewHolder.findViewById(R.id.loveButton);
            dislikeButton = viewHolder.findViewById(R.id.dislikeButton);

            //drawables
            like = mContext.getDrawable(R.drawable.like_sign_outlined);
            liked = mContext.getDrawable(R.drawable.liked_icone);
            love = mContext.getDrawable(R.drawable.love_icone_empty);
            loved = mContext.getDrawable(R.drawable.love_icone_filled);
            dislike = mContext.getDrawable(R.drawable.dislike_empty);
            disliked = mContext.getDrawable(R.drawable.dislike_icone_filled);
        }

        //tested and works fine
        private void likeCurrentComment(@NonNull Comments comment, @NonNull final String reaction) {
            final String postId = comment.getPostId();
            final String userId = currentUser.getUid();
            final String commentId = comment.getComment_id();
            Log.d(TAG, "likeCurrentComment: currentUser= " + userId);
            Log.d(TAG, "likeCurrentComment: currentCommentID= " + commentId);
            final DatabaseReference commentsReactionsRef = dbSingleton.getCommentsReactionsRef();
            final Runnable commentReactionRunnable = () -> {
                final Query query = commentsReactionsRef.child(commentId).orderByKey().equalTo(userId);
                final CommentReaction commentReaction = new CommentReaction(postId, userId, commentId, reaction);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            commentsReactionsRef.child(commentId).child(userId).setValue(commentReaction).addOnSuccessListener(aVoid -> {
                                commentReactionList.add(commentReaction);
                                Log.d(TAG, "onSuccess: successfully added");
                                Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
                                switch (reaction) {
                                    case "like":
                                        likeButton.setImageDrawable(liked);
                                        break;
                                    case "love":
                                        loveButton.setImageDrawable(loved);
                                        break;
                                    case "dislike":
                                        dislikeButton.setImageDrawable(disliked);
                                        break;
                                }

                            }).addOnFailureListener(e -> {
                                Log.d(TAG, "likeCurrentComment onFailure: failed: " + e.getMessage());
                            });
                        } else {
                            removeLike(postId, commentId, reaction);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "likeCurrentComment onCancelled: databaseError: " + databaseError.getMessage());
                    }
                });
            };
            executors.execute(commentReactionRunnable);

        }

        //tested and works fine
        private void removeLike(@NonNull String postId, @NonNull String comId, @NonNull String react) {
            final DatabaseReference commentLikeRef = dbSingleton.getCommentsReactionsRef();
            final String userId = currentUser.getUid();
            final CommentReaction commentReaction = new CommentReaction(postId, userId, comId, react);

            commentLikeRef.child(comId).child(userId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    commentReactionList.remove(commentReaction);
                    Log.d(TAG, "removeLike: task is successful: " + task.isSuccessful());
                    switch (react) {
                        case "like":
                            likeButton.setImageDrawable(like);
                            Log.d(TAG, "removeLike: like removed: " + task.isSuccessful());
                            break;
                        case "love":
                            loveButton.setImageDrawable(love);
                            Log.d(TAG, "removeLike: love removed: " + task.isSuccessful());
                            break;
                        case "dislike":
                            dislikeButton.setImageDrawable(dislike);
                            Log.d(TAG, "removeLike: dislike removed: " + task.isSuccessful());
                            break;
                    }

                }
            }).addOnFailureListener(e ->
                    Log.d(TAG, "removeLike onFailure: Exception: " + e.getMessage()));
        }
    }


}
//            commentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if (!dataSnapshot.exists()) {
//                        final CommentReaction commentReaction = new CommentReaction(commentId, userId, reaction);
//                        commentsRef.child(commentId).child(userId).setValue(commentReaction).addOnSuccessListener(aVoid -> {
//                            Log.d(TAG, "likeCurrentComment onSuccess: ");
//                            Toast.makeText(mContext, "Comment liked successfully", Toast.LENGTH_SHORT).show();
//                            reactionList.add(commentReaction);
//                            myadapter.notifyDataSetChanged();
//                            switch (reaction) {
//                                case "like":
//                                    likeButton.setImageDrawable(liked);
//                                    break;
//                                case "love":
//                                    loveButton.setImageDrawable(loved);
//                                    break;
//                                case "dislike":
//                                    dislikeButton.setImageDrawable(disliked);
//                                    break;
//                            }
//                        }).addOnFailureListener(e -> {
//                            Log.d(TAG, "likeCurrentComment onFailure: Exception: " + e.getMessage());
//
//                        });
//                    } else {
//                        final CommentReaction commentReaction = dataSnapshot.getValue(CommentReaction.class);
//                        Log.d(TAG, "onDataChange: commentReaction.commentId: " + commentReaction.getComment_id());
//                        Log.d(TAG, "onDataChange: commentReaction.reaction: " + commentReaction.getReaction());
//                        removeLike(commentReaction);
//                    }
//                }
//
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//                    Log.d(TAG, "likeCurrentComment onCancelled: DatabaseError: " + databaseError.getMessage());
//                }
//            });

//        private void bundleFunctionality(Fragment livingConditions) {
//            Bundle bundle = new Bundle();
////            String plant_name = mPlantName.getText().toString();
////            String plant_description = mPlantDescription.getText().toString();
////            String plant_profile_pic = profilePictureUrl;
////
////            bundle.putString("name", plant_name);
////            bundle.putString("description", plant_description);
////            bundle.putString("profilePictureUrl", plant_profile_pic);
////            bundle.putString("minSun", minSun);
////            bundle.putString("maxSun", maxSun);
////            bundle.putString("minTemp", minTemp);
////            bundle.putString("maxTemp", maxTemp);
////            bundle.putString("minHumidity", minHumidity);
////            bundle.putString("maxHumidity", maxHumidity);
////
////            livingConditions.setArguments(bundle);
//
//        }