package com.cnnranderson.yotzy.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
    @Bind(R.id.score)
    TextView scoreTextView;
    @Bind(R.id.highscore)
    TextView highscore;
    @Bind(R.id.lowscore)
    TextView lowscore;
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
        rand = new Random();
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
        initPreviousScores();
        initDice();
        initGameBoard();
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_main;
    }

    @OnClick(R.id.roll_dice)
    public void rollDice() {
        if (rollsLeft > 0) {
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
                            rollDiceButton.setText("Next Round! (3)");
                        } else {
                            rollDiceButton.setText("Roll Dice! (" + rollsLeft + ")");
                        }
                    })
                    .delay(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        } else if (rollsLeft == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.StyledDialog);

            builder.setMessage("You must choose a combo before rolling again!")
                    .setTitle("Turn Not Yet Played")
                    .setNegativeButton("OK", (dialog1, which) -> dialog1.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.StyledDialog);

            builder.setMessage("Would you like to play again?")
                    .setTitle("Game Over")
                    .setNegativeButton("No", (dialog1, which) -> dialog1.dismiss())
                    .setPositiveButton("Yes", (dialog2, which) -> {
                        resetDice();
                        resetBoard();
                        initGameBoard();
                        rollsLeft = 3;
                        newGameStart = true;
                        rollDiceButton.setText("Start Game! (3)");
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @OnClick({R.id.die1, R.id.die2, R.id.die3, R.id.die4, R.id.die5})
    public void dieSelected(ImageButton die) {
        if (rollsLeft != 3) {
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
    }

    @OnClick({R.id.one_score, R.id.two_score, R.id.three_score, R.id.four_score, R.id.five_score,
            R.id.six_score, R.id.threekind_score, R.id.fourkind_score, R.id.fullhouse_score,
            R.id.smstr_score, R.id.lgstr_score, R.id.chance_score, R.id.yotzy_score})
    public void chooseScore(Button combo) {
        if (combo.getText().equals("") && !newGameStart && rollsLeft != 3) {
            int value = fetchDiceValueForCombo(combo.getTag().toString());

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.StyledDialog);

            builder.setMessage("You will receive " + value + " points.")
                    .setTitle("Choose " + combo.getTag().toString() + "?")
                    .setNegativeButton("No", (dialog1, which) -> dialog1.dismiss())
                    .setPositiveButton("Yes", (dialog2, which) -> {
                        combo.setText("" + value);
                        score += value;
                        rollsLeft = 3;
                        combos.put(combo.getTag().toString(), value);
                        scoreTextView.setText("" + score);
                        if (!checkWin()) {
                            rollDiceButton.setText("Next Round! (3)");
                        } else {
                            rollsLeft = -1;
                            rollDiceButton.setText("Game Over!");
                            saveScore();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void initPreviousScores() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sp.contains("low")) {
            int val = sp.getInt("low", -1);
            if (val == -1) {
                lowscore.setText("");
            } else {
                lowscore.setText("" + val);
            }
        }

        if (sp.contains("high")) {
            int val = sp.getInt("high", -1);
            if (val == -1) {
                highscore.setText("");
            } else {
                highscore.setText("" + val);
            }
        }
    }

    private void initDice() {
        if ((rollsLeft == 0 || rollsLeft == 3) && !newGameStart) {
            rollDiceButton.setText("Next Round! (3)");
        } else if (newGameStart) {
            rollDiceButton.setText("Start Game! (3)");
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
            setScore(combo, combos.get(combo));
        }
        scoreTextView.setText("" + score);
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
                anim.setDuration(600);
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

    private void resetBoard() {
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
        score = 0;
        scoreTextView.setText("0");
    }

    private void setScore(String comboName, int value) {
        Button tv = getComboBox(comboName);
        if (tv != null) {
            if (value == -1) {
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

    private void saveScore() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sp.contains("low")) {
            if (sp.getInt("low", 99999) > score) {
                sp.edit().putInt("low", score).apply();
                lowscore.setText("" + score);
            }
        } else {
            sp.edit().putInt("low", score).apply();
            lowscore.setText("" + score);
        }

        if (sp.contains("high")) {
            if (sp.getInt("high", 0) < score) {
                sp.edit().putInt("high", score).apply();
                highscore.setText("" + score);
            }
        } else {
            sp.edit().putInt("high", score).apply();
            highscore.setText("" + score);
        }
    }

    private int fetchDiceValueForCombo(String combo) {
        switch (combo) {
            case "one_score":
                return checkSingle(1);
            case "two_score":
                return checkSingle(2);
            case "three_score":
                return checkSingle(3);
            case "four_score":
                return checkSingle(4);
            case "five_score":
                return checkSingle(5);
            case "six_score":
                return checkSingle(6);
            case "threekind_score":
                return checkMulti(3);
            case "fourkind_score":
                return checkMulti(4);
            case "fullhouse_score":
                return checkFullHouse();
            case "smstr_score":
                return checkStraight(0);
            case "lgstr_score":
                return checkStraight(1);
            case "chance_score":
                return diceSum();
            case "yotzy_score":
                if (checkYotzy()) {
                    return 50;
                } else {
                    return 0;
                }
            default:
                return 0;
        }
    }

    private boolean checkWin() {
        for (int val : combos.values()) {
            if (val == -1) {
                return false;
            }
        }
        return true;
    }

    private boolean checkYotzy() {
        int num = diceNums[0];
        for (int i : diceNums) {
            if (i != num) {
                return false;
            }
        }
        return true;
    }

    private int checkSingle(int id) {
        int sum = 0;
        for (int i : diceNums) {
            if (i + 1 == id) {
                sum += id;
            }
        }
        return sum;
    }

    private int checkMulti(int id) {
        for (int i = 1; i < 7; i++) {
            if (getDiceCount(i) >= id) {
                return diceSum();
            }
        }
        return 0;
    }

    private int checkStraight(int id) {
        switch (id) {
            case 0:
                for (int i = 0; i < 3; i++) {
                    if (contains(i) && contains(i + 1) && contains(i + 2) && contains(i + 3)) {
                        return 30;
                    }
                }
                return 0;
            case 1:
                if ((contains(0) && contains(1) && contains(2)
                        && contains(3) && contains(4))
                        || (contains(1) && contains(2) && contains(3)
                        && contains(4) && contains(5))) {
                    return 40;
                }
                return 0;
            default:
                return 0;
        }
    }

    private boolean contains(int key) {
        for (int i : diceNums) {
            if (i == key) {
                return true;
            }
        }
        return false;
    }

    private int getDiceCount(int id) {
        int count = 0;
        for (int i : diceNums) {
            if (i == id) {
                count++;
            }
        }
        return count;
    }

    private int diceSum() {
        int sum = 0;
        for (int i : diceNums) {
            sum += i + 1;
        }
        return sum;
    }

    private int checkFullHouse() {
        int id_two = -1;
        int id_three = -1;
        for (int i : diceNums) {
            if (getDiceCount(i) == 2) {
                id_two = i;
            }
            if (getDiceCount(i) == 3) {
                id_three = i;
            }
        }
        if (id_two != -1 && id_three != -1) {
            return 25;
        } else {
            return 0;
        }
    }
}
