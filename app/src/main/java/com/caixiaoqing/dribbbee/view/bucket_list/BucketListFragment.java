package com.caixiaoqing.dribbbee.view.bucket_list;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.dribbble.Dribbble;
import com.caixiaoqing.dribbbee.model.Bucket;
import com.caixiaoqing.dribbbee.view.base.DribbbeeAsyncTask;
import com.caixiaoqing.dribbbee.view.base.DribbbeeException;
import com.caixiaoqing.dribbbee.view.base.InfiniteAdapter;
import com.caixiaoqing.dribbbee.view.base.SpaceItemDecoration;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by caixiaoqing on 21/12/16.
 */

public class BucketListFragment extends Fragment {

    public static final int REQ_CODE_NEW_BUCKET = 100;
    public static final int REQ_CODE_OPEN_BUCKET = 101;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.swipe_refresh_container) SwipeRefreshLayout swipeRefreshLayout;

    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_CHOOSING_MODE = "choose_mode";
    public static final String KEY_CHOSEN_BUCKET_IDS = "chosen_bucket_ids";
    public static final String KEY_COLLECTED_BUCKET_IDS = "collected_bucket_ids";
    public static final String KEY_BUCKET_ID = "bucket_id";
    public static final String KEY_BUCKET_NAME = "bucket_name";

    BucketListAdapterOld adapter_old;
    BucketListAdapter adapter;
    private String userId;
    private boolean isChoosingMode;
    private Set<String> collectedBucketIdSet;
    //private List<String> chosenBucketIds;

    private InfiniteAdapter.LoadMoreListener onLoadMore = new InfiniteAdapter.LoadMoreListener() {
        @Override
        public void onLoadMore() {
            AsyncTaskCompat.executeParallel(new LoadBucketsTask(false));
        }
    };

    public static BucketListFragment newInstance(@Nullable String userId,
                                                 boolean isChoosingMode,
                                                 @Nullable ArrayList<String> chosenBucketIds) {
        Bundle args = new Bundle();
        args.putString(KEY_USER_ID, userId);
        args.putBoolean(KEY_CHOOSING_MODE, isChoosingMode);
        args.putStringArrayList(KEY_COLLECTED_BUCKET_IDS, chosenBucketIds);

        BucketListFragment fragment = new BucketListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipe_fab_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // get arguments
        final Bundle args = getArguments();
        userId = args.getString(KEY_USER_ID);

        isChoosingMode = args.getBoolean(KEY_CHOOSING_MODE);
        if (isChoosingMode) {
            List<String> chosenBucketIds = getArguments().getStringArrayList(KEY_COLLECTED_BUCKET_IDS);
            if (chosenBucketIds != null) {
                collectedBucketIdSet = new HashSet<>(chosenBucketIds);
            }
        } else {
            collectedBucketIdSet = new HashSet<>();
        }

        // init UI : when swipeRefreshLayout.setOnRefreshListener will be called?
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AsyncTaskCompat.executeParallel(new LoadBucketsTask(true /*refresh*/));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.spacing_medium)));

        //Bucket 3.1 : load User's bucket
        adapter = new BucketListAdapter(getContext(), new ArrayList<Bucket>(), onLoadMore, isChoosingMode);
        recyclerView.setAdapter(adapter);

        //Bucket 3.2 : create User's bucket
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewBucketDialogFragment dialogFragment = NewBucketDialogFragment.newInstance();
                dialogFragment.setTargetFragment(BucketListFragment.this, REQ_CODE_NEW_BUCKET);
                dialogFragment.show(getFragmentManager(), NewBucketDialogFragment.TAG);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_NEW_BUCKET && resultCode == Activity.RESULT_OK) {
            String bucketName = data.getStringExtra(NewBucketDialogFragment.KEY_BUCKET_NAME);
            String bucketDescription = data.getStringExtra(NewBucketDialogFragment.KEY_BUCKET_DESCRIPTION);
            if (!TextUtils.isEmpty(bucketName)) {
                AsyncTaskCompat.executeParallel(new NewBucketTask(bucketName, bucketDescription));
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isChoosingMode) {
            inflater.inflate(R.menu.bucket_list_choose_mode_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            ArrayList<String> chosenBucketIds = new ArrayList<>();
            for (Bucket bucket : adapter.getData()) {
                if (bucket.isChoosing) {
                    chosenBucketIds.add(bucket.id);
                }
            }

            Intent result = new Intent();
            result.putStringArrayListExtra(KEY_CHOSEN_BUCKET_IDS, chosenBucketIds);
            getActivity().setResult(Activity.RESULT_OK, result);
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoadBucketsTask extends DribbbeeAsyncTask<Void, Void, List<Bucket>> {

        private boolean refresh;

        public LoadBucketsTask(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected List<Bucket> doJob(Void... params) throws DribbbeeException {
            final int page = refresh ? 1 : adapter.getData().size() / Dribbble.COUNT_PER_LOAD + 1;
            return userId == null
                    ? Dribbble.getUserBuckets(page)
                    : Dribbble.getUserBuckets(userId, page); //NOTE not be used yet.
        }

        @Override
        protected void onSuccess(List<Bucket> buckets) {
            adapter.setShowLoading(buckets.size() >= Dribbble.COUNT_PER_LOAD);

            for (Bucket bucket : buckets) {
                if (collectedBucketIdSet.contains(bucket.id)) {
                    bucket.isChoosing = true;
                }
            }

            if (refresh) {
                adapter.setData(buckets);
                swipeRefreshLayout.setRefreshing(false);
            } else {
                adapter.append(buckets);
            }

            swipeRefreshLayout.setEnabled(true);
        }

        @Override
        protected void onFailed(DribbbeeException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private class NewBucketTask extends DribbbeeAsyncTask<Void, Void, Bucket> {

        private String name;
        private String description;

        private NewBucketTask(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        protected Bucket doJob(Void... params) throws DribbbeeException {
            return Dribbble.newBucket(name, description);
        }

        @Override
        protected void onSuccess(Bucket bucket) {
            bucket.isChoosing = true;
            adapter.prepend(Collections.singletonList(bucket));
        }

        @Override
        protected void onFailed(DribbbeeException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }
}
