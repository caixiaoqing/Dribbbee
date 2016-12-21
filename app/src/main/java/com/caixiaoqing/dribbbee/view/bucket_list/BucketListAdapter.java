package com.caixiaoqing.dribbbee.view.bucket_list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.model.Bucket;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caixiaoqing on 21/12/16.
 */

public class BucketListAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_BUCKET = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private List<Bucket> data;
    private LoadMoreListener loadMoreListener;
    private boolean isChoosingMode;
    private boolean showLoading;

    public BucketListAdapter(@NonNull List<Bucket> data,
                             @NonNull LoadMoreListener loadMoreListener,
                             boolean isChoosingMode) {
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.isChoosingMode = isChoosingMode;
        this.showLoading = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_BUCKET) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_bucket, parent, false);
            return new BucketViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_loading, parent, false);
            return new RecyclerView.ViewHolder(view) {};
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // note the warning for "final int position", it's for recycler view drag and drop
        // after drag and drop onBindViewHolder will not be call again with the new position,
        // that's why you should not assume this position is always fixed.

        // in our case, we do not support drag and drop in bucket list because Dribbble API
        // doesn't support reordering buckets, so using "final int position" is fine

        final int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_LOADING) {
            loadMoreListener.onLoadMore();
        } else {
            final Bucket bucket = data.get(position);
            BucketViewHolder bucketViewHolder = (BucketViewHolder) holder;

            Context context = holder.itemView.getContext();

            // 0 -> 0 shot
            // 1 -> 1 shot
            // 2 -> 2 shots
            String bucketShotCountString = MessageFormat.format(
                    context.getResources().getString(R.string.shot_count),
                    bucket.shots_count);

            bucketViewHolder.bucketName.setText(bucket.name);
            bucketViewHolder.bucketShotCount.setText(bucketShotCountString);

            if (isChoosingMode) {
                bucketViewHolder.bucketChosen.setVisibility(View.VISIBLE);
                bucketViewHolder.bucketChosen.setImageDrawable(
                        bucket.isChoosing
                                ? ContextCompat.getDrawable(context, R.drawable.ic_check_box_black_24dp)
                                : ContextCompat.getDrawable(context, R.drawable.ic_check_box_outline_blank_black_24dp));
                bucketViewHolder.bucketLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bucket.isChoosing = !bucket.isChoosing;
                        notifyItemChanged(position);
                    }
                });
            } else {
                bucketViewHolder.bucketChosen.setVisibility(View.GONE);
                bucketViewHolder.bucketLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO
                        // if not in choosing mode, we need to open a new Activity to show
                        // what shots are in this bucket, we will need ShotListFragment here!
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return showLoading ? data.size() + 1 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position < data.size()
                ? VIEW_TYPE_BUCKET
                : VIEW_TYPE_LOADING;
    }

    public void append(@NonNull List<Bucket> moreBuckets) {
        data.addAll(moreBuckets);
        notifyDataSetChanged();
    }

    public void prepend(@NonNull List<Bucket> data) {
        this.data.addAll(0, data);
        notifyDataSetChanged();
    }

    public int getDataCount() {
        return data.size();
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    @NonNull
    public ArrayList<String> getSelectedBucketIds() {
        ArrayList<String> selectedBucketIds = new ArrayList<>();
        for (Bucket bucket : data) {
            if (bucket.isChoosing) {
                selectedBucketIds.add(bucket.id);
            }
        }
        return selectedBucketIds;
    }

    public interface LoadMoreListener {
        void onLoadMore();
    }
}
