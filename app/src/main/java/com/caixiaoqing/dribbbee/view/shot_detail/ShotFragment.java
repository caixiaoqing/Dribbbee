package com.caixiaoqing.dribbbee.view.shot_detail;

import android.app.Activity;
import android.content.Context;
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
import com.caixiaoqing.dribbbee.view.base.DribbbeeAsyncTask;
import com.caixiaoqing.dribbbee.view.base.DribbbeeException;
import com.caixiaoqing.dribbbee.view.bucket_list.BucketListActivity;
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
    public static final String KEY_REMOVE_SHOT = "shot_rm";

    public static final int REQ_CODE_BUCKET = 100;
    public static final int REQ_CODE_LIKE = 101;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private ShotAdapter adapter;
    private Shot shot;
    private boolean isLiking;
    private ArrayList<String> collectedBucketIds;

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
    }

    //Step 2: onViewCreated -> setAdapter, setLayoutManager
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        shot = ModelUtils.toObject(getArguments().getString(KEY_SHOT),
                new TypeToken<Shot>(){});

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ShotAdapter(this, shot);
        recyclerView.setAdapter(adapter);

        isLiking = true;
        AsyncTaskCompat.executeParallel(new CheckLikeTask());
        //Bucket 2.3 : to load buckets => so ShotAdapter know if it should be bucketed or not
        //set collectedBucketIds
        AsyncTaskCompat.executeParallel(new LoadBucketsTask());
    }

    //Bucket 2.2 : to add/delete bucket  == handle result come back from bucket() <-- BucketListActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_BUCKET && resultCode == Activity.RESULT_OK) {
            List<String> chosenBucketIds = data.getStringArrayListExtra(
                    BucketListFragment.KEY_CHOSEN_BUCKET_IDS);
            List<String> addedBucketIds = new ArrayList<>();
            List<String> removedBucketIds = new ArrayList<>();
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

    //Bucket 2.1 : go to Choosing Bucket
    public void bucket() {
        if (collectedBucketIds == null) {
            Snackbar.make(getView(), R.string.shot_detail_loading_buckets, Snackbar.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(getContext(), BucketListActivity.class);
            intent.putExtra(BucketListFragment.KEY_CHOOSING_MODE, true);
            intent.putStringArrayListExtra(BucketListFragment.KEY_COLLECTED_BUCKET_IDS,
                    collectedBucketIds);
            startActivityForResult(intent, REQ_CODE_BUCKET);
        }
    }

    public void like(@NonNull String shotId, boolean like) {
        if (!isLiking) {
            isLiking = true;
            AsyncTaskCompat.executeParallel(new LikeTask(shotId, like));
        }
    }

    public void share(Context context) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shot.title + " " + shot.html_url);
        shareIntent.setType("text/plain");

        //receiver is activity
        //context.startActivity(shareIntent);

        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_shot)));
    }

    //
    //-----------------------------------------------
    //AsyncTasks
    //
    private class LikeTask extends DribbbeeAsyncTask<Void, Void, Void> {

        private String id;
        private boolean like;

        public LikeTask(String id, boolean like) {
            this.id = id;
            this.like = like;
        }

        @Override
        protected Void doJob(Void... params) throws DribbbeeException {
            if (like) {
                Dribbble.likeShot(id);
            } else {
                Dribbble.unlikeShot(id);
            }
            return null;
        }

        @Override
        protected void onSuccess(Void s) {
            isLiking = false;

            shot.liked = like;
            shot.likes_count += like ? 1 : -1;
            adapter.notifyDataSetChanged(); //recyclerView.getAdapter().notifyDataSetChanged();

            setResult(!like);
        }

        @Override
        protected void onFailed(DribbbeeException e) {
            isLiking = false;
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }


    private class CheckLikeTask extends DribbbeeAsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doJob(Void... params) throws DribbbeeException {
            return Dribbble.isLikingShot(shot.id);
        }

        @Override
        protected void onSuccess(Boolean result) {
            isLiking = false;
            shot.liked = result;
            adapter.notifyDataSetChanged(); //VS recyclerView.getAdapter().notifyDataSetChanged(); ?
        }

        @Override
        protected void onFailed(DribbbeeException e) {
            isLiking = false;
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private class LoadBucketsTask extends DribbbeeAsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doJob(Void... params) throws DribbbeeException {
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
        }

        @Override
        protected void onSuccess(List<String> result) {
            collectedBucketIds = new ArrayList<>(result);

            if (result.size() > 0) {
                shot.bucketed = true;
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }

        @Override
        protected void onFailed(DribbbeeException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private class UpdateBucketTask extends DribbbeeAsyncTask<Void, Void, Void> {

        private List<String> added;
        private List<String> removed;

        private UpdateBucketTask(@NonNull List<String> added,
                                 @NonNull List<String> removed) {
            this.added = added;
            this.removed = removed;
        }

        @Override
        protected Void doJob(Void... params) throws DribbbeeException {
            for (String addedId : added) {
                Dribbble.addBucketShot(addedId, shot.id);
            }

            for (String removedId : removed) {
                Dribbble.removeBucketShot(removedId, shot.id);
            }
            return null;
        }

        @Override
        protected void onSuccess(Void aVoid) {
            collectedBucketIds.addAll(added);
            collectedBucketIds.removeAll(removed);

            shot.bucketed = !collectedBucketIds.isEmpty();
            shot.buckets_count += added.size() - removed.size();

            recyclerView.getAdapter().notifyDataSetChanged();

            setResult(removed.size() > 0);
        }

        @Override
        protected void onFailed(DribbbeeException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    //Be called in LikeTask() & UpdateBucketTask(), getActivity().setResult() for ShotListFragment to update
    private void setResult(boolean removeFromList) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_SHOT, ModelUtils.toString(shot, new TypeToken<Shot>(){}));
        resultIntent.putExtra(KEY_REMOVE_SHOT, removeFromList);
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
    }
}
