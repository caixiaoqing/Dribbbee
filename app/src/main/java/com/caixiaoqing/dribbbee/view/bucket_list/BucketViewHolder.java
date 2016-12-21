package com.caixiaoqing.dribbbee.view.bucket_list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.view.base.BaseViewHolder;

import butterknife.BindView;

/**
 * Created by caixiaoqing on 21/12/16.
 */

public class BucketViewHolder extends BaseViewHolder {

    @BindView(R.id.bucket_layout) View bucketLayout;
    @BindView(R.id.bucket_name) TextView bucketName;
    @BindView(R.id.bucket_shot_count) TextView bucketShotCount;
    @BindView(R.id.bucket_shot_chosen) ImageView bucketChosen;

    public BucketViewHolder(View itemView) {
        super(itemView);
    }
}
