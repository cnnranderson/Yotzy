package com.cnnranderson.yotzy.injection.modules;

import com.cnnranderson.yotzy.Yotzy;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private Yotzy mApplication;

    public AppModule(Yotzy application) {
        this.mApplication = application;
    }

    @Provides
    @Singleton
    public Yotzy provideApplication() {
        return mApplication;
    }
}
