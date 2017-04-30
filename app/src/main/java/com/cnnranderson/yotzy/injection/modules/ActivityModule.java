package com.cnnranderson.yotzy.injection.modules;

import android.app.Activity;

import com.cnnranderson.yotzy.injection.components.PerActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

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
