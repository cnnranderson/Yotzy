package com.cnnranderson.yotzy.domain.model;

import java.util.Random;

public class DiceShelf {

    public int[] diceValues;
    public boolean[] diceLocked;

    public DiceShelf() {
        diceValues = new int[]{
                1, 1, 1, 1, 1
        };
        diceLocked = new boolean[5];
    }

    public void rollDice() {
        Random rand = new Random();
        for(int i = 0; i < diceValues.length; i++) {
            if(!diceLocked[i]) {
                diceValues[i] = rand.nextInt(5) + 1;
            }
        }
    }

    public void setDiceValues(int[] diceValues) {
        this.diceValues = diceValues.clone();
    }
    public void setDiceValues(int i1, int i2, int i3, int i4, int i5) {
        this.diceValues = new int[]{i1, i2, i3, i4, i5};
    }
}
