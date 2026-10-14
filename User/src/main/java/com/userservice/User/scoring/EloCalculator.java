package com.userservice.User.scoring;

/**
 * Elo rating maths, kept free of Spring/JPA so it is easy to unit test.
 *
 * How it works:
 *  1. Expected score: the chance side A beats side B, based on the rating gap.
 *     Equal ratings -> 0.5. A 400-point advantage -> about 0.91.
 *  2. After the match each player moves by K * (actual - expected),
 *     where actual is 1 for a win and 0 for a loss.
 *     Beating a stronger side gains more than beating a weaker one.
 *  3. K is larger for players with few games, so new players reach their
 *     true level quickly, then settles down so ratings don't swing wildly.
 *
 * Doubles: each side's strength is the average rating of its players.
 */
public final class EloCalculator {

    /** Players with fewer games than this use the higher, "provisional" K-factor. */
    public static final int PROVISIONAL_GAMES = 30;
    public static final int PROVISIONAL_K = 40;
    public static final int ESTABLISHED_K = 20;

    /** Ratings never drop below this, so a bad streak can't produce silly numbers. */
    public static final int RATING_FLOOR = 100;

    private EloCalculator() {
    }

    /** Probability (0-1) that a side rated {@code rating} beats a side rated {@code opponentRating}. */
    public static double expectedScore(double rating, double opponentRating) {
        return 1.0 / (1.0 + Math.pow(10, (opponentRating - rating) / 400.0));
    }

    public static int kFactor(int gamesPlayed) {
        return gamesPlayed < PROVISIONAL_GAMES ? PROVISIONAL_K : ESTABLISHED_K;
    }

    /**
     * Rating change for one player.
     *
     * @param teamRating     average rating of the player's side
     * @param opponentRating average rating of the other side
     * @param gamesPlayed    games the player had played before this one (picks K)
     * @param won            whether the player's side won
     */
    public static int ratingChange(double teamRating, double opponentRating, int gamesPlayed, boolean won) {
        double expected = expectedScore(teamRating, opponentRating);
        double actual = won ? 1.0 : 0.0;
        return (int) Math.round(kFactor(gamesPlayed) * (actual - expected));
    }

    public static int applyChange(int rating, int delta) {
        return Math.max(RATING_FLOOR, rating + delta);
    }
}
