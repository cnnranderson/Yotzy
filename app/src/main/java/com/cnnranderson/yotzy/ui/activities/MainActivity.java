package com.cnnranderson.yotzy.ui.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cnnranderson.yotzy.R;
import com.cnnranderson.yotzy.ui.fragments.GameFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if(savedInstanceState == null) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().add(R.id.fragment_container, new GameFragment()).commit();
        }
    }
}
