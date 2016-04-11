package com.cnnranderson.yotzy.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

import com.cnnranderson.yotzy.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class GameFragment extends BaseFragment{

    @Bind({
            R.id.die1,
            R.id.die2,
            R.id.die3,
            R.id.die4,
            R.id.die5
    })
    List<ImageButton> diceButtons;

    @Bind(R.id.roll_dice)
    Button rollDiceButton;

    public GameFragment() {}

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_main;
    }

    @OnClick(R.id.roll_dice)
    public void rollDice() {
        Observable.from(diceButtons)
                .doOnNext(this::showHideDice)
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void showHideDice(View die) {
        // get the final radius for the clipping circle
        int finalRadius = Math.max(die.getWidth(), die.getHeight()) / 2;
        int cx = die.getMeasuredWidth() / 2;
        int cy = die.getMeasuredHeight() / 2;

        // create the animator for this view (the start radius is zero)
        Animator anim;
        if(die.getVisibility() == View.INVISIBLE) {
            anim = ViewAnimationUtils.createCircularReveal(die, cx, cy, 0, finalRadius);
            die.setAlpha(1);
            die.setVisibility(View.VISIBLE);
            anim.start();
        } else {
            die.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.dice_elastic_translate));
            die.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    die.setVisibility(View.INVISIBLE);
                }}).start();
        }
    }
}
