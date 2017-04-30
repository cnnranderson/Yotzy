package com.cnnranderson.yotzy.injection.components;

import com.cnnranderson.yotzy.injection.modules.ActivityModule;
import com.cnnranderson.yotzy.injection.modules.DomainModule;
import com.cnnranderson.yotzy.module.main.MainActivity;
import com.cnnranderson.yotzy.module.game.GameFragment;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(
        modules = {
                ActivityModule.class,
                DomainModule.class
        }
)
public interface ActivityComponent {

    // Activity injections
    void inject(MainActivity activity);

    // Fragment injections
    void inject(GameFragment fragment);
}
