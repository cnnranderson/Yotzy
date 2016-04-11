package com.cnnranderson.yotzy.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

import com.cnnranderson.yotzy.R;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.OnClick;
import icepick.State;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class GameFragment extends BaseFragment {

    // Binded Views
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
    @Bind({
            R.id.one_score,
            R.id.two_score,
            R.id.three_score,
            R.id.four_score,
            R.id.five_score,
            R.id.six_score,
            R.id.threekind_score,
            R.id.fourkind_score,
            R.id.fullhouse_score,
            R.id.smstr_score,
            R.id.lgstr_score,
            R.id.chance_score,
            R.id.yotzy_score
    })
    List<Button> gridScoreButtons;

    // Persisted game vars
    @State
    boolean[] diceHeld;
    @State
    int[] diceNums;
    @State
    int rollsLeft = 3;
    @State
    int score = 0;
    @State
    HashMap<String, Integer> combos;
    @State
    boolean newGameStart = true;

    // Misc. Vars
    Random rand;

    public GameFragment() {
        diceHeld = new boolean[]{false, false, false, false, false};
        diceNums = new int[]{0, 0, 0, 0, 0};
        combos = new HashMap<>();
        combos.put("one_score", -1);
        combos.put("two_score", -1);
        combos.put("three_score", -1);
        combos.put("four_score", -1);
        combos.put("five_score", -1);
        combos.put("six_score", -1);
        combos.put("threekind_score", -1);
        combos.put("fourkind_score", -1);
        combos.put("fullhouse_score", -1);
        combos.put("smstr_score", -1);
        combos.put("lgstr_score", -1);
        combos.put("chance_score", -1);
        combos.put("yotzy_score", -1);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rand = new Random();
        initDice();
        initGameBoard();
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_main;
    }

    @OnClick(R.id.roll_dice)
    public void rollDice() {
        newGameStart = false;
        rollDiceButton.setEnabled(false);
        if (rollsLeft == 3) {
            resetDice();
        }
        rollsLeft--;
        Observable.from(diceButtons)
                .doOnNext(this::showHideDice)
                .finallyDo(() -> {
                    if (rollsLeft == 0) {
                        rollsLeft = 3;
                        rollDiceButton.setText("Next Round! (3)");
                    } else {
                        rollDiceButton.setText("Roll Dice! (" + rollsLeft + ")");
                    }
                })
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @OnClick({R.id.die1, R.id.die2, R.id.die3, R.id.die4, R.id.die5})
    public void dieSelected(ImageButton die) {
        CharSequence desc = die.getContentDescription();
        int diePos = Character.getNumericValue(desc.charAt(1));
        if (desc.charAt(0) == '0') {
            die.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimaryLight), PorterDuff.Mode.MULTIPLY);
            die.setContentDescription("1" + desc.charAt(1));
            diceHeld[diePos] = true;
        } else {
            die.clearColorFilter();
            die.setContentDescription("0" + desc.charAt(1));
            diceHeld[diePos] = false;
        }
    }

    @OnClick({R.id.one_score, R.id.two_score, R.id.three_score, R.id.four_score, R.id.five_score,
            R.id.six_score, R.id.threekind_score, R.id.fourkind_score, R.id.fullhouse_score,
            R.id.smstr_score, R.id.lgstr_score, R.id.chance_score, R.id.yotzy_score})
    public void chooseScore(Button combo) {
        int value = fetchDiceValueForCombo(combo.getTag().toString());
    }

    private void initDice() {
        if ((rollsLeft == 0 || rollsLeft == 3) && !newGameStart) {
            rollsLeft = 3;
            rollDiceButton.setText("Next Round! (3)");
        } else {
            rollDiceButton.setText("Roll Dice! (" + rollsLeft + ")");
        }

        for (int i = 0; i < 5; i++) {
            ImageButton die = diceButtons.get(i);
            die.setImageDrawable(getActivity().getDrawable(getDiceImage(diceNums[i])));
            if (diceHeld[i]) {
                die.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimaryLight), PorterDuff.Mode.MULTIPLY);
                die.setContentDescription("1" + i);
            } else {
                die.setContentDescription("0" + i);
            }
        }
    }

    private void initGameBoard() {
        for (String combo : combos.keySet()) {
            System.out.println(combo + combos.get(combo));
            setScore(combo, combos.get(combo));
        }
    }

    private void showHideDice(ImageButton die) {
        CharSequence desc = die.getContentDescription();
        if (desc.charAt(0) == '0') {
            // get the final radius for the clipping circle
            int finalRadius = Math.max(die.getWidth(), die.getHeight()) / 2;
            int cx = die.getMeasuredWidth() / 2;
            int cy = die.getMeasuredHeight() / 2;

            // create the animator for this view (the start radius is zero)
            Animator anim;
            if (die.getVisibility() == View.INVISIBLE) {
                int newNum = rand.nextInt(600) % 6;
                int diePos = Character.getNumericValue(desc.charAt(1));
                diceNums[diePos] = newNum;
                die.setImageDrawable(getActivity().getDrawable(getDiceImage(newNum)));

                anim = ViewAnimationUtils.createCircularReveal(die, cx, cy, 0, finalRadius);
                anim.setDuration(800);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        rollDiceButton.setEnabled(true);
                    }
                });
                die.setVisibility(View.VISIBLE);
                anim.start();
            } else {
                die.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.dice_elastic_translate));
                die.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        die.setVisibility(View.INVISIBLE);
                        die.postDelayed(() -> showHideDice(die), 1000);
                    }
                }).start();
            }
        } else {
            rollDiceButton.setEnabled(true);
        }
    }

    private int getDiceImage(int num) {
        switch (num) {
            case 0:
                return R.drawable.ic_dice_1_white_48dp;
            case 1:
                return R.drawable.ic_dice_2_white_48dp;
            case 2:
                return R.drawable.ic_dice_3_white_48dp;
            case 3:
                return R.drawable.ic_dice_4_white_48dp;
            case 4:
                return R.drawable.ic_dice_5_white_48dp;
            case 5:
                return R.drawable.ic_dice_6_white_48dp;
            default:
                return R.drawable.ic_dice_1_white_48dp;
        }
    }

    private void resetDice() {
        for (int i = 0; i < diceHeld.length; i++) {
            diceHeld[i] = false;
            ImageButton die = diceButtons.get(i);
            die.clearColorFilter();
            CharSequence desc = die.getContentDescription();
            die.setContentDescription("0" + desc.charAt(1));
        }
    }

    private void setScore(String comboName, int value) {
        Button tv = getComboBox(comboName);
        if (tv != null) {
            if(value == -1) {
                tv.setText("");
            } else {
                tv.setText("" + value);
            }
        }
    }

    private Button getComboBox(String comboName) {
        for (Button tv : gridScoreButtons) {
            String IdAsString = tv.getTag().toString();
            if (IdAsString.contains(comboName)) {
                return tv;
            }
        }
        return null;
    }

    private int fetchDiceValueForCombo(String combo) {
        switch(combo) {

        }
    }
}
