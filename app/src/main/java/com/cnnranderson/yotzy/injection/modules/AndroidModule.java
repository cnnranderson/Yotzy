package com.cnnranderson.yotzy.injection.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.view.inputmethod.InputMethodManager;

import com.cnnranderson.yotzy.Yotzy;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidModule {

    private final Yotzy mApplication;

    public AndroidModule(Yotzy application) {
        this.mApplication = application;
    }

    @Provides
    Application providesApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    Context providesContext() {
        return mApplication.getApplicationContext();
    }

    @Provides
    SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    InputMethodManager provideInputMethodManager(Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }
}
