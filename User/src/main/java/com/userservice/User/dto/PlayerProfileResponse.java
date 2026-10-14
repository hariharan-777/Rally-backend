package com.userservice.User.dto;

import com.userservice.User.model.Sport;

import java.util.List;
import java.util.UUID;

/** Everything another player needs to decide whether to play with this person. */
public record PlayerProfileResponse(
        UUID userId,
        String username,
        int reliabilityScore,
        String reliabilityTier,
        List<SportProfile> sports) {

    /**
     * Per-sport stats. skillRating (Elo) is null until the player has a recorded match in that sport;
     * the peer averages are null until someone has rated them in that sport.
     */
    public record SportProfile(
            Sport sport,
            Integer skillRating,
            int gamesPlayed,
            int wins,
            int losses,
            Double averagePeerSkill,
            Double averageSportsmanship,
            long peerRatingCount) {
    }
}
