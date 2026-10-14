package com.userservice.User.dto;

import com.userservice.User.model.PeerRating;
import com.userservice.User.model.Sport;

import java.time.Instant;
import java.util.UUID;

public record PeerRatingResponse(
        UUID id,
        UUID raterId,
        UUID rateeId,
        String gameId,
        Sport sport,
        int skill,
        int sportsmanship,
        String comment,
        Instant createdAt) {

    public static PeerRatingResponse from(PeerRating r) {
        return new PeerRatingResponse(r.getId(), r.getRaterId(), r.getRateeId(), r.getGameId(),
                r.getSport(), r.getSkillScore(), r.getSportsmanshipScore(), r.getComment(), r.getCreatedAt());
    }
}
