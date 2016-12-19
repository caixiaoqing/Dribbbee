package com.caixiaoqing.dribbbee.view.shot_detail;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by caixiaoqing on 18/12/16.
 */

class ImageViewHolder extends RecyclerView.ViewHolder{

    ImageView image;

    public ImageViewHolder(View itemView) {
        super(itemView);
        image = (ImageView) itemView;
    }
}
