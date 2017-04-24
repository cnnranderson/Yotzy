package com.cnnranderson.yotzy.injection.components;

import com.cnnranderson.yotzy.Yotzy;
import com.cnnranderson.yotzy.injection.modules.ActivityModule;
import com.cnnranderson.yotzy.injection.modules.AndroidModule;
import com.cnnranderson.yotzy.injection.modules.AppModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                AppModule.class,
                AndroidModule.class
        }
)
public interface AppComponent {

    ActivityComponent plus(ActivityModule module);

    Yotzy getApplication();

    void inject(Yotzy application);
}
