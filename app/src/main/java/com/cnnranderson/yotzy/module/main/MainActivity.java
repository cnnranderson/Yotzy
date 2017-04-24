package com.cnnranderson.yotzy.module.main;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cnnranderson.yotzy.R;
import com.cnnranderson.yotzy.module.base.BaseActivity;
import com.cnnranderson.yotzy.module.game.GameFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if(savedInstanceState == null) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().add(R.id.fragment_container, GameFragment.newInstance()).commit();
        }
    }
}
