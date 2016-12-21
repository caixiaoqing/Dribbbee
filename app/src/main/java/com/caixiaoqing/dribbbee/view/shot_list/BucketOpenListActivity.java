package com.caixiaoqing.dribbbee.view.shot_list;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.view.base.SingleFragmentActivity;
import com.caixiaoqing.dribbbee.view.bucket_list.BucketListFragment;

/**
 * Created by caixiaoqing on 21/12/16.
 */

public class BucketOpenListActivity extends SingleFragmentActivity {
    @NonNull
    @Override
    protected Fragment newFragment() {
        boolean isLikingFragment = false;
        String id = getIntent().getStringExtra(BucketListFragment.KEY_BUCKET_ID);
        String name = getIntent().getStringExtra(BucketListFragment.KEY_BUCKET_NAME);

        Fragment fragment = ShotListFragment.newInstance(isLikingFragment, id);
        setTitle(name);
        return fragment;
    }

}
