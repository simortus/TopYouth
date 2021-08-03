package com.example.topyouth.view_utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.example.topyouth.molde.Comments;
import com.example.topyouth.molde.Likes;
import com.example.topyouth.molde.PostModel;
import com.example.topyouth.molde.TopUser;
import com.example.topyouth.utility_classes.DBSingelton;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Comparator;
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

    //database
    private FirebaseUser currentUser;
    private FirebaseAuthSingleton singleton;
    private DBSingelton dbSingelton = DBSingelton.getInstance();
    private DatabaseReference postRef = dbSingelton.getPostsRef(),
            userRef = dbSingelton.getUsers_ref(),
            commentRef = dbSingelton.getCommentsRef(),
            likeRef = dbSingelton.getLikesRef();

    private FirebaseDatabase database = dbSingelton.getDbInstance();

    // threads
    private ExecutorService executors = Executors.newCachedThreadPool();


    public RecyclerViewAdapter(@NonNull final Activity mContext, @NonNull List<PostModel> postsList,
                               @NonNull List<TopUser> postOwnerList) {
        this.mContext = mContext;
        this.postsList = postsList;
        this.postOwnerList = postOwnerList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);


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
        if (postsList.size() > 0) {
            PostModel currentPost = postsList.get(position);
            if (postOwnerList.size() > 0) {
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
                        holder.likeImageButton.setImageDrawable(mContext.getDrawable(R.drawable.like_sign));
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
//            final Button doneButton = layoutView.findViewById(R.id.doneButton_dialog);

            final List<TopUser> commentators = new ArrayList<>();
            final List<Comments> commentsList = new ArrayList<>();
            final ListViewAdapter myadapter = new ListViewAdapter(mContext, commentsList, commentators);

            //Dialog constructor and setup
            final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            final AlertDialog alertDialog = dialog.create();
            alertDialog.setCanceledOnTouchOutside(false);

            WindowManager.LayoutParams wlp = alertDialog.getWindow().getAttributes();
            wlp.windowAnimations = R.style.Widget_AppCompat_Light_ListPopupWindow;
            wlp.gravity = Gravity.CENTER;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            alertDialog.getWindow().setAttributes(wlp);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            dialog.setNegativeButton("Done", (dialogInterface, i) -> dialogInterface.dismiss());

//            doneButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    alertDialog.dismiss();
//                }
//            });

            dialog.setView(layoutView);

            //Data  and layout setup
            final String postID = postsList.get(getAdapterPosition()).getPostId();
            final Query commentQuery = commentRef.child(postID);
            commentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChildren()) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                final String userId = ds.getKey();
                                Log.d(TAG, "onDataChange: userId: " + userId);
                                fetchUser(userId, commentators, myadapter);
                                for (DataSnapshot dss : ds.getChildren()) {
                                    final Comments comment = dss.getValue(Comments.class);
                                    commentsList.add(comment);
                                    Log.d(TAG, "onDataChange: commentSnapshot id: " + comment.getComment_id());
                                    Log.d(TAG, "onDataChange: commentSnapshot comment text: " + comment.getCommentText());
                                }
                            }

                            listView.setAdapter(myadapter);
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
            dialog.show();


        }

        private void fetchUser(final String userId, List<TopUser> userList, ListViewAdapter adapter) {
            final Query userQuery = userRef.child(userId);
            userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        final TopUser currentuser = dataSnapshot.getValue(TopUser.class);
                        userList.add(currentuser);
                        adapter.notifyDataSetChanged();

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
//            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
                                likeImageButton.setImageDrawable(mContext.getDrawable(R.drawable.like_sign));
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

            Runnable commentRun = () -> {
                final Comments comment = new Comments(user_id, comment_id, commentDate, commentText);
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


    class ListViewAdapter extends ArrayAdapter<Comments> {
        private static final String TAG = "ListViewAdapter";

        private final List<Comments> commentList;
        private final List<TopUser> commnters;
        private final Context mContext;

        @SuppressLint("ResourceType")
        public ListViewAdapter(@NonNull final Context context, final List<Comments> commentList, final List<TopUser> commenters) {
            super(context, R.layout.comment_display_layout, commentList);
            this.mContext = context;
            this.commentList = commentList;
            this.commnters = commenters;

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
            final CircularImageView commenterProfile = viewHolder.findViewById(R.id.commenter_profilePic);
            final TextView usernameTextView = viewHolder.findViewById(R.id.username);
            final TextView commentTextView = viewHolder.findViewById(R.id.comment_textView);
            final TextView commentDateTextView = viewHolder.findViewById(R.id.comment_date);

            if (commentTextView == null) {
                Log.d(TAG, "getView: what the fuck!");
            }

            if (commentList.size() > 0) {

                Comments currentComment = commentList.get(position);
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
            return viewHolder;
        }
    }


}


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