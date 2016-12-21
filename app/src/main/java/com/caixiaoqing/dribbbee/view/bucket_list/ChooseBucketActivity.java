package com.caixiaoqing.dribbbee.view.bucket_list;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.view.base.SingleFragmentActivity;

import java.util.ArrayList;

/**
 * Created by caixiaoqing on 21/12/16.
 */

public class ChooseBucketActivity extends SingleFragmentActivity {

    @NonNull
    @Override
    protected Fragment newFragment() {
        // TODO: we need to pass in the chosen bucket ids to BucketListFragment here
        return BucketListFragment.newInstance(true, new ArrayList<String>());
    }

    @NonNull
    @Override
    protected String getActivityTitle() {
        return getString(R.string.choose_bucket);
    }
}
