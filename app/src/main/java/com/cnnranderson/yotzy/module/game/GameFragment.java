package com.cnnranderson.yotzy.module.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cnnranderson.yotzy.R;
import com.cnnranderson.yotzy.injection.Injector;
import com.cnnranderson.yotzy.module.base.BaseFragment;
import com.cnnranderson.yotzy.module.base.BasePresenter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import icepick.State;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class GameFragment extends BaseFragment {

    @Inject
    GamePresenter mPresenter;
    @Inject
    GoogleApiClient mGoogleApiClient;

    // Binded Views
    @BindView(R.id.roll_dice)
    Button rollDiceButton;
    @BindView(R.id.score)
    TextView scoreTextView;
    @BindView(R.id.highscore)
    TextView highscore;
    @BindView(R.id.lowscore)
    TextView lowscore;
    @BindViews({ R.id.die1, R.id.die2, R.id.die3, R.id.die4, R.id.die5 })
    List<ImageButton> diceButtons;
    @BindViews({
            R.id.one_score, R.id.two_score, R.id.three_score,
            R.id.four_score, R.id.five_score, R.id.six_score,
            R.id.threekind_score, R.id.fourkind_score,
            R.id.fullhouse_score, R.id.smstr_score, R.id.lgstr_score,
            R.id.chance_score, R.id.yotzy_score
    })
    List<Button> gridScoreButtons;

    // Persisted game vars
    @State boolean newGameStart = true;
    @State boolean[] diceHeld;
    @State int[] diceNums;
    @State int rollsLeft = 3;
    @State int score = 0;
    @State HashMap<String, Integer> combos;

    // Misc. Vars
    private Random rand;

    public static Fragment newInstance() {
        GameFragment fragment = new GameFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void injectFragment() {
        Injector.getActivityScopeInjector((AppCompatActivity) getActivity()).inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup game
        diceHeld = new boolean[]{ false, false, false, false, false };
        diceNums = new int[]{ 0, 0, 0, 0, 0 };
        rand = new Random();

        // Init Combo score map
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

        initPreviousScores();
        initDice();
        initGameBoard();
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected Unbinder bindViews(View view) {
        return ButterKnife.bind(this, view);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @OnClick(R.id.roll_dice)
    public void rollDice() {
        if (rollsLeft > 0) {
            rollDiceButton.setEnabled(false);
            newGameStart = false;

            // Reset dice for new round
            if (rollsLeft == 3) {
                resetDice();
            }
            rollsLeft--;

            // Roll the dice!

            Observable.just(diceButtons)
                    .flatMapIterable(diceButtons -> diceButtons)
                    .doOnNext(this::showHideDice)
                    .doFinally(() -> {
                        if (rollsLeft == 0) {
                            rollDiceButton.setText("Next Round! (3)");
                        } else {
                            rollDiceButton.setText("Roll Dice! (" + rollsLeft + ")");
                        }
                    })
                    .delay(500, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.io())
                    .subscribe();

        } else if (rollsLeft == 0) {
            // Force player to make a move before continuing to next round
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.StyledDialog);

            builder.setMessage("You must choose a combo before rolling again!")
                    .setTitle("Turn Not Yet Played")
                    .setNegativeButton("OK", (dialog1, which) -> dialog1.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            // Game over -- Ask to continue playing
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.StyledDialog);

            builder.setMessage("Would you like to play again?")
                    .setTitle("Game Over")
                    .setNegativeButton("No", (dialog1, which) -> dialog1.dismiss())
                    .setPositiveButton("Yes", (dialog2, which) -> {
                        // Refresh game board
                        resetDice();
                        resetBoard();
                        initGameBoard();

                        // Setup roll button again
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
        // Lock a die in place to hold over to next roll
        if (rollsLeft != 3) {
            // TODO: Change from using content description to data tag
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

    /**
     * Select a combo to place current dice score into.
     * Dialog prompt will show to help confirm choice, along
     * with displaying the number of points their choice will allot
     * them.
     * @param combo Name of combo that was selected
     */
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

    /**
     * Collect lowest and highest score and display them
     */
    private void initPreviousScores() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sp.contains("low")) {
            int val = sp.getInt("low", -1);
            String saveScore = String.valueOf(val);
            if (val == -1) {
                lowscore.setText("");
            } else {
                lowscore.setText(saveScore);
            }
        }

        if (sp.contains("high")) {
            int val = sp.getInt("high", -1);
            String saveScore = String.valueOf(val);
            if (val == -1) {
                highscore.setText("");
            } else {
                highscore.setText(saveScore);
            }
        }
    }

    /**
     * Initialize the dice and the roll button according to the game state
     */
    private void initDice() {
        String rollButtonText;
        if ((rollsLeft == 0 || rollsLeft == 3) && !newGameStart) {
            rollButtonText = "Next Round! (3)";
        } else if (newGameStart) {
            rollButtonText = "Start Game! (3)";
        } else {
            rollButtonText = "Roll Dice! (" + rollsLeft + ")";
        }
        rollDiceButton.setText(rollButtonText);

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

    /**
     * Initialize the combo game board
     */
    private void initGameBoard() {
        for (String combo : combos.keySet()) {
            setScore(combo, combos.get(combo));
        }
        String saveScore = String.valueOf(score);
        scoreTextView.setText(saveScore);
    }

    /**
     * Animate the dice when a roll is made. Dice
     * that are "held" will not be animated.
     * @param die ImageButton die to be animated
     */
    private void showHideDice(ImageButton die) {
        CharSequence desc = die.getContentDescription();
        if (desc.charAt(0) == '0') {
            // Get the final radius for the clipping circle
            int finalRadius = Math.max(die.getWidth(), die.getHeight()) / 2;
            int cx = die.getMeasuredWidth() / 2;
            int cy = die.getMeasuredHeight() / 2;

            // Create the animator for this view (the start radius is zero)
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

    /**
     * Gets the respective dice image depending on the number given.
     * @param num Id of dice to retrieve
     * @return Drawable integer id
     */
    private int getDiceImage(int num) {
        switch (num) {
            case 5: return R.drawable.ic_dice_6_white_48dp;
            case 4: return R.drawable.ic_dice_5_white_48dp;
            case 3: return R.drawable.ic_dice_4_white_48dp;
            case 2: return R.drawable.ic_dice_3_white_48dp;
            case 1: return R.drawable.ic_dice_2_white_48dp;
            case 0:
            default:
                return R.drawable.ic_dice_1_white_48dp;
        }
    }

    /**
     * Resets all the dice to non-held position
     */
    private void resetDice() {
        for (int i = 0; i < diceHeld.length; i++) {
            diceHeld[i] = false;
            ImageButton die = diceButtons.get(i);
            die.clearColorFilter();
            CharSequence desc = die.getContentDescription();
            die.setContentDescription("0" + desc.charAt(1));
        }
    }

    /**
     * Resets all the combo scores and current points
     */
    private void resetBoard() {
        // Reset combos
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

        // Reset score
        score = 0;
        scoreTextView.setText("0");
    }

    /**
     * Sets the combo button to the evaluated number from choosing
     * it, based on the current dice rolled.
     * @param comboName Name of selected combo
     * @param value Value to be placed in combo box
     */
    private void setScore(String comboName, int value) {
        Button tv = getComboBox(comboName);
        if (tv != null) {
            if (value == -1) {
                tv.setText("");
            } else {
                String stringValue = String.valueOf(value);
                tv.setText(stringValue);
            }
        }
    }

    /**
     * Retrieves a button with the given combo name
     * @param comboName Name of combo button to be retrieved
     * @return Combo Button
     */
    private Button getComboBox(String comboName) {
        for (Button tv : gridScoreButtons) {
            String IdAsString = tv.getTag().toString();
            if (IdAsString.contains(comboName)) {
                return tv;
            }
        }
        return null;
    }

    /**
     * Saves score upon winning the game to sharedpreferences
     */
    private void saveScore() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String saveScore = String.valueOf(score);
        if (sp.contains("low")) {
            if (sp.getInt("low", Integer.MAX_VALUE) > score) {
                sp.edit().putInt("low", score).apply();
                lowscore.setText(saveScore);
            }
        } else {
            sp.edit().putInt("low", score).apply();
            lowscore.setText(saveScore);
        }

        if (sp.contains("high")) {
            if (sp.getInt("high", 0) < score) {
                sp.edit().putInt("high", score).apply();
                highscore.setText(saveScore);
            }
        } else {
            sp.edit().putInt("high", score).apply();
            highscore.setText(saveScore);
        }
    }

    /**
     * Check for a full game board
     * @return true if board is full, false otherwise
     */
    private boolean checkWin() {
        for (int val : combos.values()) {
            if (val == -1) {
                return false;
            }
        }
        return true;
    }


    /////////////////////////////////////////
    ////                                 ////
    ////     Dice Evaluation Methods     ////
    ////                                 ////
    /////////////////////////////////////////
    /**
     * Fetches the value achieved from choosing a certain combo
     * @param combo Combo chosen
     * @return Value of dice from choosing the combo
     */
    private int fetchDiceValueForCombo(String combo) {
        switch (combo) {
            case "one_score": return checkSingle(1);
            case "two_score": return checkSingle(2);
            case "three_score": return checkSingle(3);
            case "four_score": return checkSingle(4);
            case "five_score": return checkSingle(5);
            case "six_score": return checkSingle(6);
            case "threekind_score": return checkMulti(3);
            case "fourkind_score": return checkMulti(4);
            case "fullhouse_score": return checkFullHouse();
            case "smstr_score": return checkStraight(0);
            case "lgstr_score": return checkStraight(1);
            case "chance_score": return diceSum();
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

    /**
     * Check if player has rolled a Yotzy
     * @return true if dice are in yotzy form, false otherwise
     */
    private boolean checkYotzy() {
        int num = diceNums[0];
        for (int i : diceNums) {
            if (i != num) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the value of the dice for a given dice number
     * @param id Number to sum up
     * @return sum of all the id's in the current dice roll
     */
    private int checkSingle(int id) {
        int sum = 0;
        for (int i : diceNums) {
            if (i + 1 == id) {
                sum += id;
            }
        }
        return sum;
    }

    /**
     * Gets the value of n-of-a-kind rolls
     * @param id Amount of dice needed to be matched
     * @return Sum of all dice, if n-of-a-kind was found
     */
    private int checkMulti(int id) {
        for (int i = 1; i < 7; i++) {
            if (getDiceCount(i) >= id) {
                return diceSum();
            }
        }
        return 0;
    }

    /**
     * Search for consecutive numbers ('Straights'):
     * Small Straight - 1-4, 2-5, 3-6
     * Large Straight - 1-5, 2-6
     * @param id
     *      Id of straight to search for:
     *      0 for small straight, 1 for large straight search
     * @return 30 for small straight, 40 for large straight, and 0 otherwise
     */
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

    /**
     * Check if the dice contain a certain value
     * TODO: Very ineffective, as far as complexity is concerned
     * @param key Dice number to look for
     * @return true if key is found, false otherwise
     */
    private boolean contains(int key) {
        for (int i : diceNums) {
            if (i == key) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the count of die with the given id
     * @param id number to look for
     * @return number of instances that id appeared in the current roll of dice
     */
    private int getDiceCount(int id) {
        int count = 0;
        for (int i : diceNums) {
            if (i == id) {
                count++;
            }
        }
        return count;
    }

    /**
     * Sums all the dice currently rolled
     * @return sum of all dice
     */
    private int diceSum() {
        int sum = 0;
        for (int i : diceNums) {
            sum += i + 1;
        }
        return sum;
    }

    /**
     * Searches for 3-2 rolls, where there are 3 of one number, and 2 of another
     * @return 25 if full house is found, 0 otherwise
     */
    private int checkFullHouse() {
        int id_two = -1;
        int id_three = -1;
        for (int i : diceNums) {
            // Search for exactly two dice with the same number
            if (id_two == -1 && getDiceCount(i) == 2) {
                id_two = i;
            }
            // Search for exactly three dice with the same number
            if (id_three == -1 && getDiceCount(i) == 3) {
                id_three = i;
            }
        }

        // If two dice types were found, we have a full house
        if (id_two != -1 && id_three != -1) {
            return 25;
        } else {
            return 0;
        }
    }
}
