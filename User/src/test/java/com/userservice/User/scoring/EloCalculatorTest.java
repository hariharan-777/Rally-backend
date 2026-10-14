package com.userservice.User.scoring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class EloCalculatorTest {

    @Test
    void equalRatingsGiveFiftyFiftyExpectation() {
        assertThat(EloCalculator.expectedScore(1200, 1200)).isEqualTo(0.5);
    }

    @Test
    void fourHundredPointGapIsAboutNinetyOnePercent() {
        assertThat(EloCalculator.expectedScore(1600, 1200)).isCloseTo(0.909, within(0.001));
    }

    @Test
    void newPlayersMoveTwentyPointsForAnEvenWin() {
        // K = 40 for provisional players, expected = 0.5 -> 40 * 0.5 = 20
        assertThat(EloCalculator.ratingChange(1200, 1200, 0, true)).isEqualTo(20);
        assertThat(EloCalculator.ratingChange(1200, 1200, 0, false)).isEqualTo(-20);
    }

    @Test
    void establishedPlayersMoveLess() {
        assertThat(EloCalculator.kFactor(29)).isEqualTo(40);
        assertThat(EloCalculator.kFactor(30)).isEqualTo(20);
        assertThat(EloCalculator.ratingChange(1200, 1200, 50, true)).isEqualTo(10);
    }

    @Test
    void upsetWinGainsMoreThanExpectedWin() {
        int underdogWin = EloCalculator.ratingChange(1200, 1600, 50, true);
        int favouriteWin = EloCalculator.ratingChange(1600, 1200, 50, true);
        assertThat(underdogWin).isEqualTo(18);
        assertThat(favouriteWin).isEqualTo(2);
    }

    @Test
    void ratingNeverDropsBelowFloor() {
        assertThat(EloCalculator.applyChange(110, -30)).isEqualTo(EloCalculator.RATING_FLOOR);
    }
}
