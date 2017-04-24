package com.cnnranderson.yotzy.util;

import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComboUtil {

    /////////////////////////////////////////
    ////                                 ////
    ////     Dice Evaluation Methods     ////
    ////                                 ////
    /////////////////////////////////////////

    public static int getTotalSumValue(int[] diceValues) {
        int sum = 0;
        for (int i : diceValues) {
            sum += i;
        }
        return sum;
    }

    public static boolean isYotzy(int[] diceValues) {
        int startDie = diceValues[0];
        for (int die : diceValues) {
            if (die != startDie) {
                return false;
            }
        }
        return true;
    }

    public static int getSingleSumValue(int singleType, int[] diceValues) {
        int sum = 0;
        for (int die : diceValues) {
            if (die == singleType) {
                sum += die;
            }
        }
        return sum;
    }

    public static int getMultiSumValue(int multi, int[] diceValues) {
        List<Integer> checked = new ArrayList<>();
        for(int die : diceValues) {
            if(!checked.contains(die)) {
                checked.add(die);
                if(getFrequency(diceValues, die) >= multi) {
                    return getTotalSumValue(diceValues);
                }
            }
        }
        return 0;
    }

    private static int getFrequency(int[] diceValues, int value) {
        int count = 0;
        for(int die : diceValues) {
            if(die == value) {
                count++;
            }
        }
        return count;
    }
}
