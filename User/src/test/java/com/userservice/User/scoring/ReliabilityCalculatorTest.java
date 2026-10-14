package com.userservice.User.scoring;

import com.userservice.User.model.AttendanceStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.userservice.User.model.AttendanceStatus.ATTENDED;
import static com.userservice.User.model.AttendanceStatus.LATE_CANCELLATION;
import static com.userservice.User.model.AttendanceStatus.NO_SHOW;
import static org.assertj.core.api.Assertions.assertThat;

class ReliabilityCalculatorTest {

    @Test
    void newPlayerStartsAtNinety() {
        assertThat(ReliabilityCalculator.score(List.of())).isEqualTo(90);
        assertThat(ReliabilityCalculator.tier(90, 0)).isEqualTo("NEW");
    }

    @Test
    void singleEarlyNoShowIsNotDevastating() {
        assertThat(ReliabilityCalculator.score(List.of(NO_SHOW))).isEqualTo(75);
    }

    @Test
    void lateCancellationCountsAsHalf() {
        assertThat(ReliabilityCalculator.score(List.of(LATE_CANCELLATION))).isEqualTo(83);
    }

    @Test
    void mostlyReliablePlayerIsReliable() {
        int score = ReliabilityCalculator.score(games(19, 1));
        assertThat(score).isEqualTo(94);
        assertThat(ReliabilityCalculator.tier(score, 20)).isEqualTo("RELIABLE");
    }

    @Test
    void frequentNoShowsDropTheTier() {
        int fiveOfTwenty = ReliabilityCalculator.score(games(15, 5));
        assertThat(fiveOfTwenty).isEqualTo(78);
        assertThat(ReliabilityCalculator.tier(fiveOfTwenty, 20)).isEqualTo("FAIR");

        int tenOfTwenty = ReliabilityCalculator.score(games(10, 10));
        assertThat(tenOfTwenty).isEqualTo(58);
        assertThat(ReliabilityCalculator.tier(tenOfTwenty, 20)).isEqualTo("UNRELIABLE");
    }

    @Test
    void onlyTheLastTwentyGamesCount() {
        List<AttendanceStatus> history = new ArrayList<>(Collections.nCopies(20, ATTENDED));
        history.addAll(Collections.nCopies(30, NO_SHOW)); // older games, beyond the window
        assertThat(ReliabilityCalculator.score(history)).isEqualTo(98);
    }

    private static List<AttendanceStatus> games(int attended, int noShows) {
        List<AttendanceStatus> list = new ArrayList<>(Collections.nCopies(attended, ATTENDED));
        list.addAll(Collections.nCopies(noShows, NO_SHOW));
        return list;
    }
}
