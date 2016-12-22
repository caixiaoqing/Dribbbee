package com.caixiaoqing.dribbbee.view.bucket_list;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.caixiaoqing.dribbbee.view.base.SingleFragmentActivity;
import com.caixiaoqing.dribbbee.view.shot_list.ShotListFragment;

/**
 * Created by caixiaoqing on 22/12/16.
 */

public class BucketShotListActivity extends SingleFragmentActivity {

    public static final String KEY_BUCKET_NAME = "bucketName";

    @NonNull
    @Override
    protected String getActivityTitle() {
        return getIntent().getStringExtra(KEY_BUCKET_NAME);
    }

    @NonNull
    @Override
    protected Fragment newFragment() {
        String bucketId = getIntent().getStringExtra(ShotListFragment.KEY_BUCKET_ID);

        //TODO when bucketId is null
        return bucketId == null
                ? ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_POPULAR)
                : ShotListFragment.newBucketListInstance(bucketId);
    }
}

