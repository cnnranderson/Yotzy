package com.cnnranderson.yotzy.injection.modules;

import android.support.v7.app.AppCompatActivity;

import com.cnnranderson.yotzy.injection.components.PerActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import dagger.Module;
import dagger.Provides;

@Module
public class DomainModule {

    private final AppCompatActivity mContext;

    public DomainModule(AppCompatActivity context) {
        this.mContext = context;
    }

    @Provides
    @PerActivity
    GoogleApiClient provideGoogleApiClient() {
        return new GoogleApiClient.Builder(mContext)
                .enableAutoManage(mContext, (GoogleApiClient.OnConnectionFailedListener) mContext)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .build();
    }
}
