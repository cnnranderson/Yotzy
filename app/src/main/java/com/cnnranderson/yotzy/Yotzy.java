package com.cnnranderson.yotzy;

import android.app.Application;

import com.cnnranderson.yotzy.injection.Injector;

import timber.log.Timber;

public class Yotzy extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Start Logging
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // Start building dependency graph
        Injector.createApplicationScopeInjector(this).inject(this);
    }
}
