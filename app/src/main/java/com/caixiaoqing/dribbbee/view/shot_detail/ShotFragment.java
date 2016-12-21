package com.caixiaoqing.dribbbee.view.shot_detail;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.dribbble.Dribbble;
import com.caixiaoqing.dribbbee.model.Bucket;
import com.caixiaoqing.dribbbee.model.Shot;
import com.caixiaoqing.dribbbee.utils.ModelUtils;
import com.caixiaoqing.dribbbee.view.bucket_list.BucketListFragment;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by caixiaoqing on 18/12/16.
 */

public class ShotFragment extends Fragment {

    public static final String KEY_SHOT = "shot";
    public static final String KEY_LIKE_ID = "like_id";
    public static final String KEY_IS_LIKED = "is_liked";

    public static final int REQ_CODE_BUCKET = 100;
    public static final int REQ_CODE_LIKE = 101;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private ShotAdapter adapter;
    private Shot shot;

    //Data flow 5 :
    public static ShotFragment newInstance(@NonNull Bundle args) {
        ShotFragment fragment = new ShotFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //Step 1: onCreateView -> inflate(fragment)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    //Step 2: onViewCreated -> setAdapter, setLayoutManager
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        shot = ModelUtils.toObject(getArguments().getString(KEY_SHOT),
                new TypeToken<Shot>(){});
        adapter = new ShotAdapter(this, shot);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        //Bucket 2.3 : to load buckets => so ShotAdapter know if it should be bucketed or not
        AsyncTaskCompat.executeParallel(new LoadCollectedBucketIdsTask());

        AsyncTaskCompat.executeParallel(new LoadLikeIdsTask());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Bucket 2.2 : to add/delete bucket
        if (requestCode == REQ_CODE_BUCKET && resultCode == Activity.RESULT_OK) {
            List<String> chosenBucketIds = data.getStringArrayListExtra(BucketListFragment.KEY_CHOSEN_BUCKET_IDS);
            List<String> addedBucketIds = new ArrayList<>();
            List<String> removedBucketIds = new ArrayList<>();
            List<String> collectedBucketIds = adapter.getReadOnlyCollectedBucketIds();

            for (String chosenBucketId : chosenBucketIds) {
                if (!collectedBucketIds.contains(chosenBucketId)) {
                    addedBucketIds.add(chosenBucketId);
                }
            }

            for (String collectedBucketId : collectedBucketIds) {
                if (!chosenBucketIds.contains(collectedBucketId)) {
                    removedBucketIds.add(collectedBucketId);
                }
            }

            AsyncTaskCompat.executeParallel(new UpdateBucketTask(addedBucketIds, removedBucketIds));
        }
    }

    private class LoadCollectedBucketIdsTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                List<Bucket> shotBuckets = Dribbble.getShotBuckets(shot.id);
                List<Bucket> userBuckets = Dribbble.getUserBuckets();

                Set<String> userBucketIds = new HashSet<>();
                for (Bucket userBucket : userBuckets) {
                    userBucketIds.add(userBucket.id);
                }

                List<String> collectedBucketIds = new ArrayList<>();
                for (Bucket shotBucket : shotBuckets) {
                    if (userBucketIds.contains(shotBucket.id)) {
                        collectedBucketIds.add(shotBucket.id);
                    }
                }

                return collectedBucketIds;
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> collectedBucketIds) {
            adapter.updateCollectedBucketIds(collectedBucketIds);
        }
    }

    private class LoadLikeIdsTask extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            try {
                List<Shot> likes = Dribbble.getUserLikes();
                Map<String, String> likeIds = new HashMap<String, String>();
                for(Shot s: likes){
                    likeIds.put(s.id, s.like_id);
                }
                return likeIds;
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Map<String, String> likeIds) {
            adapter.updateLikeIdsFromLoading(likeIds);
        }
    }

    private class UpdateBucketTask extends AsyncTask<Void, Void, Void> {

        private List<String> added;
        private List<String> removed;
        private Exception e;

        private UpdateBucketTask(@NonNull List<String> added,
                                 @NonNull List<String> removed) {
            this.added = added;
            this.removed = removed;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (String addedId : added) {
                    Dribbble.addBucketShot(addedId, shot.id);
                }

                for (String removedId : removed) {
                    Dribbble.removeBucketShot(removedId, shot.id);
                }
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                this.e = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (e == null) {
                adapter.updateCollectedBucketIds(added, removed);
            } else {
                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
