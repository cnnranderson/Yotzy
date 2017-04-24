package com.cnnranderson.yotzy.module.game;

import android.content.SharedPreferences;

import com.cnnranderson.yotzy.module.base.BasePresenter;

import javax.inject.Inject;

public class GamePresenter extends BasePresenter {

    GameView mView;
    SharedPreferences mPreferences;

    @Inject
    public GamePresenter(SharedPreferences preferences) {
        this.mPreferences = preferences;
    }

    public void onViewCreated(GameView view) {
        this.mView = view;
    }
}
