package com.example.topyouth.view_utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.topyouth.R;
import com.example.topyouth.molde.Comments;
import com.example.topyouth.molde.Likes;
import com.example.topyouth.molde.PostModel;
import com.example.topyouth.molde.TopUser;
import com.example.topyouth.utility_classes.DBSingelton;
import com.google.firebase.database.DatabaseReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CardViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    //database
    private Context mContext;

    private List<PostModel> postsList;
    private List<TopUser> postOwnerList;
    private List<Likes> postLikes;
    private List<Comments> postComments;

    public RecyclerViewAdapter(Context mContext, List<PostModel> postsList, List<TopUser> postOwnerList, List<Likes> postLikes, List<Comments> postComments) {
        this.mContext = mContext;
        this.postsList = postsList;
        this.postOwnerList = postOwnerList;
        this.postLikes = postLikes;
        this.postComments = postComments;
    }


    public RecyclerViewAdapter(Context mContext, List<PostModel> postsList, List<TopUser> postOwnerList) {
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
                }
            }


            //post details
            final String url = currentPost.getImageUrl();
            Log.d(TAG, "onBindViewHolder: post_url: "+url);
            holder.postDetails.setText(currentPost.getPostDetails());
            Glide.with(this.mContext).load(url).centerCrop().into(holder.postImageView);
        }

    }


    @Override
    public int getItemCount() {
        return postsList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final String TAG = "CardViewHolder";

        private CircularImageView postUserPorfileImage, postUserStatus;
        private TextView postUsername, postDetails, postLikeNumber, postCommentNumber, likeButtonText, commentButtonText;
        private ImageButton settingImageButton, likeImageButton, commentImageButton;
        private ImageView postImageView;


        CardViewHolder(@NonNull View itemView) {
            super(itemView);

            postUserPorfileImage = itemView.findViewById(R.id.post_user_image);
            postUserStatus = itemView.findViewById(R.id.approved_sign);
            postDetails = itemView.findViewById(R.id.post_details);
            postUsername = itemView.findViewById(R.id.post_user_name);
            postLikeNumber = itemView.findViewById(R.id.post_likes_number);
            postCommentNumber = itemView.findViewById(R.id.post_comments_number);
            likeButtonText = itemView.findViewById(R.id.like_text);
            commentButtonText = itemView.findViewById(R.id.comment_text);
            settingImageButton = itemView.findViewById(R.id.post_setting);
            likeImageButton = itemView.findViewById(R.id.like_button);
            commentImageButton = itemView.findViewById(R.id.comment_button);
            postImageView = itemView.findViewById(R.id.post_image);


        }


        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.post_setting:
                    Log.d(TAG, "onClick: post_setting clicked");
                    break;
                case R.id.like_button:
                    Log.d(TAG, "onClick: like_button clicked");
                    break;

                case R.id.comment_button:
                    Log.d(TAG, "onClick: comment_button clicked");
                    break;

            }

        }


        /**
         * Method for passing information about a plant to a fragment
         */
        private void bundleFunctionality(Fragment livingConditions) {
            Bundle bundle = new Bundle();
//            String plant_name = mPlantName.getText().toString();
//            String plant_description = mPlantDescription.getText().toString();
//            String plant_profile_pic = profilePictureUrl;
//
//            bundle.putString("name", plant_name);
//            bundle.putString("description", plant_description);
//            bundle.putString("profilePictureUrl", plant_profile_pic);
//            bundle.putString("minSun", minSun);
//            bundle.putString("maxSun", maxSun);
//            bundle.putString("minTemp", minTemp);
//            bundle.putString("maxTemp", maxTemp);
//            bundle.putString("minHumidity", minHumidity);
//            bundle.putString("maxHumidity", maxHumidity);
//
//            livingConditions.setArguments(bundle);

        }

    }
}
