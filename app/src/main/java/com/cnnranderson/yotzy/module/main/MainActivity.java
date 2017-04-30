package com.cnnranderson.yotzy.module.main;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.cnnranderson.yotzy.R;
import com.cnnranderson.yotzy.injection.Injector;
import com.cnnranderson.yotzy.module.base.BaseActivity;
import com.cnnranderson.yotzy.module.game.GameFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // TODO: Create fragment management/navigation
        if(savedInstanceState == null) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().add(R.id.fragment_container, GameFragment.newInstance()).commit();
        }
    }

    @Override
    public void injectActivity() {
        Injector.getActivityScopeInjector(this).inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("Connecting to Google Play API");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()) {
            Timber.d("Disconnecting from Google Play API");
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.e("Connection to Google Play API Failed!");
    }
}
