package com.cnnranderson.yotzy.module.base;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BasePresenter {

    protected CompositeDisposable mDisposable;

    public void onBaseViewCreated() {
        mDisposable = new CompositeDisposable();
    }

    public void onBaseViewDestroyed() {
        mDisposable.clear();
    }

}
