package com.caixiaoqing.dribbbee.view.shot_detail;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.caixiaoqing.dribbbee.view.base.SingleFragmentActivity;

/**
 * Created by caixiaoqing on 18/12/16.
 */

public class ShotActivity extends SingleFragmentActivity {

    public static final String KEY_SHOT_TITLE = "shot_title";

    @NonNull
    @Override
    protected Fragment newFragment() {
        //Data flow 4 : ShotActivity(getIntent) -> ShotFragment.newInstance()
        return ShotFragment.newInstance(getIntent().getExtras());
    }

    @NonNull
    @Override
    protected String getActivityTitle() {
        return getIntent().getStringExtra(KEY_SHOT_TITLE);
    }
}
