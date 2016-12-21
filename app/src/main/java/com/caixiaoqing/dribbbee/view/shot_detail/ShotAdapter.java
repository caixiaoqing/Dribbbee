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
import android.widget.Toast;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.dribbble.Dribbble;
import com.caixiaoqing.dribbbee.model.Like;
import com.caixiaoqing.dribbbee.model.Shot;
import com.caixiaoqing.dribbbee.view.MainActivity;
import com.caixiaoqing.dribbbee.view.bucket_list.BucketListFragment;
import com.caixiaoqing.dribbbee.view.bucket_list.ChooseBucketActivity;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

                //Bucket 1.2 : bucketed or not
                Drawable bucketDrawable = shot.bucketed
                        ? ContextCompat.getDrawable(shotDetailViewHolder.itemView.getContext(),
                        R.drawable.ic_inbox_dribbble_18dp)
                        : ContextCompat.getDrawable(shotDetailViewHolder.itemView.getContext(),
                        R.drawable.ic_inbox_black_18dp);
                shotDetailViewHolder.bucketButton.setImageDrawable(bucketDrawable);

                //shot.liked ?
                Drawable likeDrawable = shot.liked
                        ? ContextCompat.getDrawable(shotDetailViewHolder.itemView.getContext(),
                        R.drawable.ic_favorite_dribbble_18dp)
                        :  ContextCompat.getDrawable(shotDetailViewHolder.itemView.getContext(),
                        R.drawable.ic_favorite_black_18dp);
                shotDetailViewHolder.likeButton.setImageDrawable(likeDrawable);

                shotDetailViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        share(v.getContext());
                    }
                });
                //Bucket 1.1 : bucket (add / delete) entry
                shotDetailViewHolder.bucketButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bucket(v.getContext());
                    }
                });


                shotDetailViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        like(v.getContext());
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

    //Bucket 2.1 : go to ShotFragment to add/delete bucket
    //TODO Who set collectedBucketIds?
    private void bucket(Context context) {
        // collectedBucketIds == null means we're still loading
        Intent intent = new Intent(context, ChooseBucketActivity.class);
        intent.putStringArrayListExtra(BucketListFragment.KEY_CHOSEN_BUCKET_IDS,
                collectedBucketIds);
        shotFragment.startActivityForResult(intent, ShotFragment.REQ_CODE_BUCKET);
    }

    private void like(Context context) {
        List<String> addedLikeIds = new ArrayList<>();
        List<String> removedLikeIds = new ArrayList<>();
        if(shot.liked) {
            removedLikeIds.add(shot.like_id);
        }
        else {
            addedLikeIds.add(shot.id);
        }
        AsyncTaskCompat.executeParallel(new UpdateLikeTask(addedLikeIds, removedLikeIds));
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

    //TODO how data pass to ShotListFragment
    public void updateLikeIds(@NonNull List<String> added, @NonNull List<String> removed) {
        if(added.isEmpty()){
            shot.liked = false;
            shot.likes_count--;
        }
        else {
            shot.liked = true;
            shot.likes_count++;
        }
        notifyDataSetChanged();
    }

    public void updateLikeIdsFromLoading(@NonNull Map<String, String> likeIds) {
        if(likeIds.containsKey(shot.id)) {
            shot.liked = true;
            shot.like_id = likeIds.get(shot.id);
        }
        if(this.likeIds == null) {
            this.likeIds = new ArrayList<>();
        }
        likeIds.clear();
        for(Map.Entry<String, String> entry : likeIds.entrySet()) {
            this.likeIds.add(entry.getKey());
        }
        notifyDataSetChanged();
    }

    //TODO how to move it to Fragment
    private class UpdateLikeTask extends AsyncTask<Void, Void, Void> {

        private List<String> added;
        private List<String> removed;
        private Exception e;

        private UpdateLikeTask(@NonNull List<String> added,
                               @NonNull List<String> removed) {
            this.added = added;
            this.removed = removed;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (String addedId : added) {
                    Like like = Dribbble.addLikeShot(shot.id);
                    shot.like_id = like.id;
                }

                for (String removedId : removed) {
                    Dribbble.removeLikeShot(shot.id);
                }
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                this.e = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (e == null) {
                updateLikeIds(added, removed);
            } else {
                Snackbar.make(shotFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
