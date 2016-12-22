package com.caixiaoqing.dribbbee.view.shot_detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.model.Shot;
import com.caixiaoqing.dribbbee.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by caixiaoqing on 18/12/16.
 */

class ShotAdapter extends RecyclerView.Adapter{

    private static final int VIEW_TYPE_SHOT_IMAGE = 0;
    private static final int VIEW_TYPE_SHOT_INFO = 1;

    private final ShotFragment shotFragment;
    private final Shot shot;
    private ArrayList<String> collectedBucketIds;
    private ArrayList<String> likeIds;

    public ShotAdapter(@NonNull ShotFragment shotFragment,
                       @NonNull Shot shot) {
        this.shotFragment = shotFragment;
        this.shot = shot;
        this.collectedBucketIds = null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_SHOT_IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shot_item_image, parent, false);
                return new ImageViewHolder(view);
            case VIEW_TYPE_SHOT_INFO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shot_item_info, parent, false);
                return new InfoViewHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_SHOT_IMAGE:
                Picasso.with(holder.itemView.getContext())
                        .load(shot.getImageUrl())
                        .placeholder(R.drawable.shot_placeholder)
                        .into(((ImageViewHolder) holder).image);
                break;
            case VIEW_TYPE_SHOT_INFO:
                InfoViewHolder shotDetailViewHolder = (InfoViewHolder) holder;
                shotDetailViewHolder.title.setText(shot.title);
                shotDetailViewHolder.authorName.setText(shot.user.name);

                shotDetailViewHolder.description.setText(Html.fromHtml(
                        shot.description == null ? "" : shot.description));
                //For open the links
                shotDetailViewHolder.description.setMovementMethod(LinkMovementMethod.getInstance());

                shotDetailViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
                shotDetailViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
                shotDetailViewHolder.viewCount.setText(String.valueOf(shot.views_count));

                ImageUtils.loadCircularUserPicture(getContext(),
                                            shotDetailViewHolder.authorPicture,
                                            shot.user.avatar_url);

                //Bucket 1.2 : bucketed or not
                Drawable bucketDrawable = shot.bucketed
                        ? ContextCompat.getDrawable(shotDetailViewHolder.itemView.getContext(), R.drawable.ic_inbox_dribbble_18dp)
                        : ContextCompat.getDrawable(shotDetailViewHolder.itemView.getContext(), R.drawable.ic_inbox_black_18dp);
                shotDetailViewHolder.bucketButton.setImageDrawable(bucketDrawable);

                //shot.liked ?
                Drawable likeDrawable = shot.liked
                        ? ContextCompat.getDrawable(shotDetailViewHolder.itemView.getContext(), R.drawable.ic_favorite_dribbble_18dp)
                        :  ContextCompat.getDrawable(shotDetailViewHolder.itemView.getContext(), R.drawable.ic_favorite_black_18dp);
                shotDetailViewHolder.likeButton.setImageDrawable(likeDrawable);


                //Bucket 1.1 : bucket (add / delete) entry
                shotDetailViewHolder.bucketButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shotFragment.bucket();
                    }
                });
                shotDetailViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shotFragment.like(shot.id, !shot.liked);
                    }
                });
                shotDetailViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shotFragment.share(v.getContext());
                    }
                });
                break;
        }
    }


    @Override
    public int getItemCount() {
        return 2;
    }

    //For mix-type RecyclerView -- getItemViewType()
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_SHOT_IMAGE;
        } else {
            return VIEW_TYPE_SHOT_INFO;
        }
    }

    @NonNull
    private Context getContext() {
        return shotFragment.getContext();
    }

}
