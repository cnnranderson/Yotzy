package com.cnnranderson.yotzy.util;

import com.cnnranderson.yotzy.domain.model.DiceShelf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ComboUtilUnitTest {

    /*
        SUM Tests
     */

    @Test
    public void ComboUtil_Value_sumValueIsCorrect() throws Exception {
        // GIVEN 5 dice with values of 1
        DiceShelf shelf = new DiceShelf();
        shelf.setDiceValues(1, 1, 1, 1, 1);

        // THEN the expected total dice value is 5
        assertEquals(5, ComboUtil.getTotalSumValue(shelf.diceValues));

        // GIVEN 5 dice with values of increasing sequence from 1
        shelf.setDiceValues(1, 2, 3, 4, 5);

        // THEN the expected total dice value is 15
        assertEquals(15, ComboUtil.getTotalSumValue(shelf.diceValues));
    }

    @Test
    public void ComboUtil_Value_sumSingleValueIsCorrect() throws Exception {
        DiceShelf shelf = new DiceShelf();
        assertEquals(5, ComboUtil.getSingleSumValue(1, shelf.diceValues));

        shelf.setDiceValues(1, 1, 1, 1, 2);
        assertEquals(4, ComboUtil.getSingleSumValue(1, shelf.diceValues));

        shelf.setDiceValues(1, 1, 1, 1, 1);
        assertEquals(0, ComboUtil.getSingleSumValue(2, shelf.diceValues));
    }

    /*
        COMBO Tests
     */

    @Test
    public void ComboUtil_Combo_isYotzy() throws Exception {
        // GIVEN five dice
        DiceShelf shelf = new DiceShelf();

        // WHERE all die are the same
        shelf.setDiceValues(1, 1, 1, 1, 1);

        // THEN the combo should be considered a yotzy
        assertEquals(true, ComboUtil.isYotzy(shelf.diceValues));
    }

    @Test
    public void ComboUtil_Combo_isNotYotzy() throws Exception {
        // GIVEN five dice
        DiceShelf shelf = new DiceShelf();

        // WHERE all die are the same
        shelf.setDiceValues(1, 1, 1, 1, 2);

        // THEN the combo shouldn't be considered a yotzy
        assertEquals(false, ComboUtil.isYotzy(shelf.diceValues));
    }

    @Test
    public void ComboUtil_Combo_isThreeOfAKind() throws Exception {
        // GIVEN five dice
        DiceShelf shelf = new DiceShelf();

        // WHERE at least three are the same
        shelf.setDiceValues(1, 1, 1, 2, 3);

        // THEN the sum should be all the total value of the dice
        assertEquals(8, ComboUtil.getMultiSumValue(3, shelf.diceValues));
    }

    @Test
    public void ComboUtil_Combo_isNotThreeOfAKind() throws Exception {
        // GIVEN five dice
        DiceShelf shelf = new DiceShelf();

        // WHERE less than three are the same
        shelf.setDiceValues(1, 1, 3, 2, 3);

        // THEN the sum should be 0
        assertEquals(0, ComboUtil.getMultiSumValue(3, shelf.diceValues));
    }
}
