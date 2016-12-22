package com.caixiaoqing.dribbbee.view.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caixiaoqing.dribbbee.R;

import java.util.List;

/**
 * Created by caixiaoqing on 22/12/16.
 */

public abstract class InfiniteAdapter<M> extends RecyclerView.Adapter<BaseViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;

    private final LoadMoreListener loadMoreListener;
    private boolean showLoading;

    private List<M> data;
    //TODO for what?
    //1. no need to parent.getContext() in onCreateViewHolder
    //2. no need holder.itemView.getContext(); in onBindItemViewHolder
    private final Context context;

    public InfiniteAdapter(@NonNull Context context,
                           @NonNull List<M> data,
                           @NonNull LoadMoreListener loadMoreListener) {
        this.context = context;
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.showLoading = true;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_loading, parent, false);
            return new BaseViewHolder(view);
        } else {
            //Note: let child do this job
            return onCreateItemViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == TYPE_LOADING) {
            loadMoreListener.onLoadMore();
        } else {
            //Note: let child do this job
            onBindItemViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return showLoading ? data.size() + 1 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoading) {
            return position == data.size() ? TYPE_LOADING : TYPE_ITEM;
        } else {
            return TYPE_ITEM;
        }
    }

    protected Context getContext() {
        return context;
    }

    public List<M> getData() {
        return data;
    }

    public void setData(@NonNull List<M> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    public void append(@NonNull List<M> data) {
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    public void prepend(@NonNull List<M> data) {
        this.data.addAll(0, data);
        notifyDataSetChanged();
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    protected abstract BaseViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType);

    protected abstract void onBindItemViewHolder(BaseViewHolder holder, int position);

    public interface LoadMoreListener {
        void onLoadMore();
    }
}
