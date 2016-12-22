package com.caixiaoqing.dribbbee.view.shot_list;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.model.Shot;
import com.caixiaoqing.dribbbee.utils.ModelUtils;
import com.caixiaoqing.dribbbee.view.shot_detail.ShotActivity;
import com.caixiaoqing.dribbbee.view.shot_detail.ShotFragment;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by caixiaoqing on 14/12/16.
 */

public class ShotListAdapterOld extends RecyclerView.Adapter{

    private static final int VIEW_TYPE_SHOT = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private List<Shot> data;
    //Infinite loading list 4: callback for onLoadMore (ShotListFragment)
    private LoadMoreListener loadMoreListener;
    private boolean showLoading;

    public ShotListAdapterOld(@NonNull List<Shot> data, @NonNull LoadMoreListener loadMoreListener) {
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.showLoading = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SHOT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_shot, parent, false);
            return new ShotViewHolder(view);
        } else {

            //Infinite loading list 2: loading xml -- list_item_loading + (no data binding)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_loading, parent, false);
            return new RecyclerView.ViewHolder(view) {};
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        //Infinite loading list 3:  3.1 data binding - shot
        //                          3.2 load more data	(in a separate thread) -> come back to UI
        if (holder instanceof ShotViewHolder) {
            final Shot shot = data.get(position);

            ShotViewHolder shotViewHolder = (ShotViewHolder) holder;
            shotViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
            shotViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
            shotViewHolder.viewCount.setText(String.valueOf(shot.views_count));

            Picasso.with(shotViewHolder.itemView.getContext())
                    .load(shot.getImageUrl())
                    .placeholder(R.drawable.shot_placeholder)
                    .into(shotViewHolder.image);

            //Go to ShotActivity
            //Data flow 2: ShotListAdapterOld (intent) -> ShotActivity (SingleFragmentActivity newFragment)
            shotViewHolder.cover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = holder.itemView.getContext();
                    Intent intent = new Intent(context, ShotActivity.class);
                    intent.putExtra(ShotFragment.KEY_SHOT,
                            ModelUtils.toString(shot, new TypeToken<Shot>() {
                            }));
                    intent.putExtra(ShotActivity.KEY_SHOT_TITLE, shot.title);
                    context.startActivity(intent);
                }
            });
        }
        else {
            //Infinite loading list 5
            loadMoreListener.onLoadMore();
        }
    }

    //Infinite loading list 1: shot + loading
    @Override
    public int getItemCount() {
        return showLoading ? data.size() + 1 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position < data.size()
                ? VIEW_TYPE_SHOT
                : VIEW_TYPE_LOADING;
    }

    //Infinite loading list 6 -> come back to UI
    public void append(@NonNull List<Shot> moreShots) {
        data.addAll(moreShots);
        notifyDataSetChanged();
    }

    public int getDataCount() {
        return data.size();
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    public interface LoadMoreListener {
        void onLoadMore();
    }
}
