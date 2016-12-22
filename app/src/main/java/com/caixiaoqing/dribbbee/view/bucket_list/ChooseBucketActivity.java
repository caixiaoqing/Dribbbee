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
        ArrayList<String> chosenBucketIds = getIntent().getStringArrayListExtra(
                BucketListFragment.KEY_CHOSEN_BUCKET_IDS);
        return BucketListFragment.newInstance(null, true, chosenBucketIds);
    }

    @NonNull
    @Override
    protected String getActivityTitle() {
        return getString(R.string.choose_bucket);
    }
}
