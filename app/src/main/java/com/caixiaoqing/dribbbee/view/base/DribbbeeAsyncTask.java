package com.caixiaoqing.dribbbee.view.base;

import android.os.AsyncTask;

/**
 * Created by caixiaoqing on 22/12/16.
 */

public abstract class DribbbeeAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>{
    private DribbbeeException exception;

    protected abstract Result doJob(Params... params) throws DribbbeeException;

    protected abstract void onSuccess(Result result);

    protected abstract void onFailed(DribbbeeException e);

    @Override
    protected Result doInBackground(Params... params) {
        try {
            return doJob(params);
        } catch (DribbbeeException e) {
            e.printStackTrace();
            exception = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (exception != null) {
            onFailed(exception);
        } else {
            onSuccess(result);
        }
    }
}
