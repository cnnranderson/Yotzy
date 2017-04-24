package com.cnnranderson.yotzy.injection.modules;

import android.app.Activity;

import com.cnnranderson.yotzy.injection.components.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private final Activity mActivity;

    public ActivityModule(Activity activity) {
        this.mActivity = activity;
    }

    @Provides
    @PerActivity
    Activity providesActivity() {
        return mActivity;
    }
}
