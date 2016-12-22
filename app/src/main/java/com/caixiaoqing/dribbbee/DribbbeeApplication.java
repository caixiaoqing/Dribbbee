package com.caixiaoqing.dribbbee;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by caixiaoqing on 22/12/16.
 */

public class DribbbeeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
