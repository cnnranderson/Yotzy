package com.cnnranderson.yotzy.injection;

import android.app.Activity;

import com.cnnranderson.yotzy.Yotzy;
import com.cnnranderson.yotzy.injection.components.ActivityComponent;
import com.cnnranderson.yotzy.injection.components.AppComponent;
import com.cnnranderson.yotzy.injection.components.DaggerAppComponent;
import com.cnnranderson.yotzy.injection.modules.ActivityModule;
import com.cnnranderson.yotzy.injection.modules.AndroidModule;
import com.cnnranderson.yotzy.injection.modules.AppModule;

public class Injector {

    private final static ScopeComponentStack<Activity, ActivityComponent> mActivityScopeComponentStack = new ScopeComponentStack<>();
    private static AppComponent mAppComponent;

    public static AppComponent createApplicationScopeInjector(Yotzy application) {
        if (mAppComponent == null) {
            mAppComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(application))
                    .androidModule(new AndroidModule(application))
                    .build();
        }
        return mAppComponent;
    }

    public static AppComponent getApplicationScopeInjector() {
        return mAppComponent;
    }

    public static ActivityComponent getActivityScopeInjector(Activity activity) {
        ActivityComponent component = mActivityScopeComponentStack.getComponent(activity);
        if (component == null) {
            component = mActivityScopeComponentStack.createComponentForScope(activity,
                    () -> mAppComponent.plus(new ActivityModule(activity)));
        }
        return component;
    }

    public static ActivityComponent getCurrentActivityScopeInjector() {
        return mActivityScopeComponentStack.getTop();
    }

    public static void releaseActivityScopeInjector(Activity activity) {
        mActivityScopeComponentStack.releaseComponent(activity);
    }
}
