package com.example.topyouth.view_utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.topyouth.R;
import com.example.topyouth.molde.PostModel;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

public class GridImageAdapter extends ArrayAdapter<PostModel> {
    private static final String TAG = "GridImageAdapter";
    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private List<PostModel> imgURLs;

    public GridImageAdapter(Context context, int layoutResource, List<PostModel> imgURLs) {
        super(context, layoutResource, imgURLs);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.layoutResource = layoutResource;
        this.imgURLs = imgURLs;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        final ViewHolder holder;
        /**
         * ViewHolder build pattern (Similar to RecyclerView)
         */
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.mProgressBar = convertView.findViewById(R.id.grid_image_progress_bar);
            holder.image = convertView.findViewById(R.id.gridImageView);
            holder.mProgressBar.setVisibility(View.VISIBLE);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.mProgressBar.setVisibility(View.VISIBLE);
        }
        holder.mProgressBar.setVisibility(View.VISIBLE);
        String photoUrl = imgURLs.get(position).getImageUrl();
        Glide.with(convertView).load(photoUrl).centerCrop().into(holder.image);
        holder.mProgressBar.setVisibility(View.INVISIBLE);
        return convertView;
    }

    private static class ViewHolder {
        ImageView image;
        ProgressBar mProgressBar;
    }

}
