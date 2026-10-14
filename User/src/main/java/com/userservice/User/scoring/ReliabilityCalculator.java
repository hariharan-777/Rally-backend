package com.userservice.User.scoring;

import com.userservice.User.model.AttendanceStatus;

import java.util.List;

/**
 * Turns a player's recent attendance into a 0-100 reliability score.
 *
 * How it works:
 *  - Each recent game earns points: attended = 1, late cancellation = 0.5, no-show = 0.
 *  - Only the last {@link #WINDOW} games count, so players can recover from old mistakes.
 *  - We add {@link #PRIOR_GAMES} imaginary games at {@link #PRIOR_RATE} reliability.
 *    This stops one early no-show from wrecking a new player's score (0/1 = 0%),
 *    and stops one attended game from giving a perfect 100%. The more real games
 *    a player has, the less the imaginary ones matter.
 *
 *    score = 100 * (points + PRIOR_GAMES * PRIOR_RATE) / (games + PRIOR_GAMES)
 *
 * Examples: new player -> 90. One no-show as first game -> 75.
 * 20 games with 1 no-show -> 94. 20 games with 5 no-shows -> 78.
 */
public final class ReliabilityCalculator {

    public static final int WINDOW = 20;
    public static final int PRIOR_GAMES = 5;
    public static final double PRIOR_RATE = 0.9;

    /** Fewer real games than this and the player is shown as NEW instead of a tier. */
    public static final int MIN_GAMES_FOR_TIER = 3;
    public static final int RELIABLE_THRESHOLD = 85;
    public static final int FAIR_THRESHOLD = 65;

    private ReliabilityCalculator() {
    }

    /** @param recent most recent first; anything beyond {@link #WINDOW} is ignored. */
    public static int score(List<AttendanceStatus> recent) {
        List<AttendanceStatus> window = recent.size() > WINDOW ? recent.subList(0, WINDOW) : recent;
        double points = 0;
        for (AttendanceStatus status : window) {
            points += status.getWeight();
        }
        double score = 100.0 * (points + PRIOR_GAMES * PRIOR_RATE) / (window.size() + PRIOR_GAMES);
        return (int) Math.round(score);
    }

    public static String tier(int score, int gamesConsidered) {
        if (gamesConsidered < MIN_GAMES_FOR_TIER) {
            return "NEW";
        }
        if (score >= RELIABLE_THRESHOLD) {
            return "RELIABLE";
        }
        if (score >= FAIR_THRESHOLD) {
            return "FAIR";
        }
        return "UNRELIABLE";
    }
}
