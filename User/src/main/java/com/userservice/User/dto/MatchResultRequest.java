package com.userservice.User.dto;

import com.userservice.User.model.Sport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Body for POST /matches/results. Singles: one id on each side. Doubles: two ids on each side.
 */
public record MatchResultRequest(
        @NotBlank(message = "required") String gameId,
        @NotNull(message = "required") Sport sport,
        @NotEmpty(message = "at least one winner required") List<UUID> winnerIds,
        @NotEmpty(message = "at least one loser required") List<UUID> loserIds) {
}
