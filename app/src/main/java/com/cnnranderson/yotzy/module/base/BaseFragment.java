package com.cnnranderson.yotzy.module.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseFragment extends Fragment {

    protected CompositeDisposable mDisposables;
    private Unbinder mUnbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDisposables = new CompositeDisposable();

        if (getPresenter() != null) {
            getPresenter().onBaseViewCreated();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        mUnbinder = bindViews(view);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Icepick.saveInstanceState(this, outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDisposables.clear();

        if (getPresenter() != null) {
            getPresenter().onBaseViewDestroyed();
        }

        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    protected abstract BasePresenter getPresenter();

    protected abstract Unbinder bindViews(View view);

    protected abstract int getLayoutId();
}