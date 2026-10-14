package com.userservice.User.dto;

import com.userservice.User.model.Sport;

import java.util.List;
import java.util.UUID;

public record MatchResultResponse(String gameId, Sport sport, List<RatingChange> changes) {

    public record RatingChange(UUID userId, boolean won, int oldRating, int newRating, int delta) {
    }
}
