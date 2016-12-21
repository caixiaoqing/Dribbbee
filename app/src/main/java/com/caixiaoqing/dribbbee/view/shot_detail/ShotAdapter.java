package com.caixiaoqing.dribbbee.view.shot_detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.model.Shot;
import com.caixiaoqing.dribbbee.view.bucket_list.BucketListFragment;
import com.caixiaoqing.dribbbee.view.bucket_list.ChooseBucketActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by caixiaoqing on 18/12/16.
 */

class ShotAdapter extends RecyclerView.Adapter{

    private static final int VIEW_TYPE_SHOT_IMAGE = 0;
    private static final int VIEW_TYPE_SHOT_INFO = 1;

    private final ShotFragment shotFragment;
    private final Shot shot;
    private ArrayList<String> collectedBucketIds;

    public ShotAdapter(@NonNull ShotFragment shotFragment,
                       @NonNull Shot shot) {
        this.shotFragment = shotFragment;
        this.shot = shot;
        this.collectedBucketIds = null;
    }

    //TODO when onCreateViewHolder will be called
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

    //TODO when onBindViewHolder will be called
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

                Drawable bucketDrawable = shot.bucketed
                        ? ContextCompat.getDrawable(shotDetailViewHolder.itemView.getContext(),
                        R.drawable.ic_inbox_dribbble_18dp)
                        : ContextCompat.getDrawable(shotDetailViewHolder.itemView.getContext(),
                        R.drawable.ic_inbox_black_18dp);
                shotDetailViewHolder.bucketButton.setImageDrawable(bucketDrawable);

                shotDetailViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        share(v.getContext());
                    }
                });
                shotDetailViewHolder.bucketButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bucket(v.getContext());
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

    private void share(Context context) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shot.title + " " + shot.html_url);
        shareIntent.setType("text/plain");

        //receiver is activity
        //context.startActivity(shareIntent);

        context.startActivity(Intent.createChooser(shareIntent,
                context.getString(R.string.share_shot)));
    }

    private void bucket(Context context) {
        // collectedBucketIds == null means we're still loading
        Intent intent = new Intent(context, ChooseBucketActivity.class);
        intent.putStringArrayListExtra(BucketListFragment.KEY_CHOSEN_BUCKET_IDS,
                collectedBucketIds);
        shotFragment.startActivityForResult(intent, ShotFragment.REQ_CODE_BUCKET);
    }

    public List<String> getReadOnlyCollectedBucketIds() {
        return Collections.unmodifiableList(collectedBucketIds);
    }

    public void updateCollectedBucketIds(@NonNull List<String> bucketIds) {
        if (collectedBucketIds == null) {
            collectedBucketIds = new ArrayList<>();
        }

        collectedBucketIds.clear();
        collectedBucketIds.addAll(bucketIds);

        shot.bucketed = !bucketIds.isEmpty();
        notifyDataSetChanged();
    }

    public void updateCollectedBucketIds(@NonNull List<String> addedIds,
                                         @NonNull List<String> removedIds) {
        if (collectedBucketIds == null) {
            collectedBucketIds = new ArrayList<>();
        }

        collectedBucketIds.addAll(addedIds);
        collectedBucketIds.removeAll(removedIds);

        shot.bucketed = !collectedBucketIds.isEmpty();
        shot.buckets_count += addedIds.size() - removedIds.size();
        notifyDataSetChanged();
    }
}
