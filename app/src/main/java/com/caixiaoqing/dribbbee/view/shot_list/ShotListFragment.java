package com.caixiaoqing.dribbbee.view.shot_list;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.dribbble.Dribbble;
import com.caixiaoqing.dribbbee.model.Shot;
import com.caixiaoqing.dribbbee.model.User;
import com.caixiaoqing.dribbbee.view.base.SpaceItemDecoration;
import com.caixiaoqing.dribbbee.view.bucket_list.BucketListFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by caixiaoqing on 14/12/16.
 */

public class ShotListFragment extends Fragment {
    private static final String KEY_LIKING_MODE = "liking_mode";
    private static final String KEY_BUCKET_ID = "bucket_id";
    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private ShotListAdapter adapter;
    private boolean isLikingMode;
    private String bucketId;

    public static ShotListFragment newInstance(boolean isLikingMode, String bucketId) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_LIKING_MODE, isLikingMode);
        args.putString(KEY_BUCKET_ID, bucketId);

        ShotListFragment fragment = new ShotListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        isLikingMode = getArguments().getBoolean(KEY_LIKING_MODE);
        bucketId = getArguments().getString(KEY_BUCKET_ID);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
               getResources().getDimensionPixelSize(R.dimen.spacing_medium)));

        //Data flow 1: ShotListFragment(generate data list) -> ShotListAdapter()
        //Infinite loading list 5: load shot + load More

        adapter = new ShotListAdapter(new ArrayList<Shot>(), new ShotListAdapter.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                // this method will be called when the RecyclerView is displayed
                // page starts from 1
                if (!isLikingMode && bucketId.isEmpty()) {
                    AsyncTaskCompat.executeParallel(new LoadShotTask(adapter.getDataCount() / Dribbble.COUNT_PER_PAGE + 1));
                }
                else if(isLikingMode){
                    AsyncTaskCompat.executeParallel(new LoadUserLikesTask(adapter.getDataCount() / Dribbble.COUNT_PER_PAGE + 1));
                }
                else {
                    AsyncTaskCompat.executeParallel(new LoadBucketTask(bucketId, adapter.getDataCount() / Dribbble.COUNT_PER_PAGE + 1));
                }

            }
        });
        recyclerView.setAdapter(adapter);
    }

    private class LoadShotTask extends AsyncTask<Void, Void, List<Shot>> {

        int page;

        public LoadShotTask(int page) {
            this.page = page;
        }

        @Override
        protected List<Shot> doInBackground(Void... params) {
            try {
                return Dribbble.getShots(page);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Shot> shots) {
            if (shots != null) {
                adapter.append(shots);
            } else {
                Snackbar.make(getView(), "Error!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class LoadUserLikesTask extends AsyncTask<Void, Void, List<Shot>> {

        int page;

        public LoadUserLikesTask(int page) {
            this.page = page;
        }

        @Override
        protected List<Shot> doInBackground(Void... params) {
            try {
                return Dribbble.getUserLikes(page);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Shot> shots) {
            if (shots != null) {
                for(Shot s : shots) {
                    s.liked = true;
                }
                adapter.append(shots);
                adapter.setShowLoading(shots.size() == Dribbble.COUNT_PER_PAGE);
            } else {
                Snackbar.make(getView(), "Error!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class LoadBucketTask extends AsyncTask<Void, Void, List<Shot>> {

        String bucketId;
        int page;

        public LoadBucketTask(String bucketId, int page) {
            this.bucketId = bucketId;
            this.page = page;
        }

        @Override
        protected List<Shot> doInBackground(Void... params) {
            try {
                return Dribbble.getBucketShots(bucketId, page);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Shot> shots) {
            if (shots != null) {
                Log.i("open bucket", String.valueOf(shots.size()));
                Log.i("open bucket", String.valueOf(shots.size()));
                Log.i("open bucket", String.valueOf(shots.size()));
                Log.i("open bucket", String.valueOf(shots.size()));
                Log.i("open bucket", String.valueOf(shots.size()));
                Log.i("open bucket", String.valueOf(shots.size()));
                Log.i("open bucket", String.valueOf(shots.size()));
                adapter.append(shots);
                adapter.setShowLoading(shots.size() == Dribbble.COUNT_PER_PAGE);
            } else {
                Snackbar.make(getView(), "Error!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private List<Shot> fakeData(int page) {
        List<Shot> shotList = new ArrayList<>();
        Random random = new Random();

        int count = page < 2 ? Dribbble.COUNT_PER_PAGE : 10;

        for (int i = 0; i < count; ++i) {
            Shot shot = new Shot();
            shot.title = "shot" + i;
            shot.views_count = random.nextInt(10000);
            shot.likes_count = random.nextInt(200);
            shot.buckets_count = random.nextInt(50);
            shot.description = makeDescription();

            shot.user = new User();
            shot.user.name = shot.title + " author";

            shotList.add(shot);
        }
        return shotList;
    }

    private static final String[] words = {
            "bottle", "bowl", "brick", "building", "bunny", "cake", "car", "cat", "cup",
            "desk", "dog", "duck", "elephant", "engineer", "fork", "glass", "griffon", "hat", "key",
            "knife", "lawyer", "llama", "manual", "meat", "monitor", "mouse", "tangerine", "paper",
            "pear", "pen", "pencil", "phone", "physicist", "planet", "potato", "road", "salad",
            "shoe", "slipper", "soup", "spoon", "star", "steak", "table", "terminal", "treehouse",
            "truck", "watermelon", "window"
    };

    private static String makeDescription() {
        return TextUtils.join(" ", words);
    }
}
