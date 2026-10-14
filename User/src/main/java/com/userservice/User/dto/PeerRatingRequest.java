package com.userservice.User.dto;

import com.userservice.User.model.Sport;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Body for POST /users/{id}/ratings - the player in the URL is the one being rated. */
public record PeerRatingRequest(
        @NotNull(message = "required") UUID raterId,
        @NotBlank(message = "required") String gameId,
        @NotNull(message = "required") Sport sport,
        @NotNull(message = "required") @Min(1) @Max(5) Integer skill,
        @NotNull(message = "required") @Min(1) @Max(5) Integer sportsmanship,
        @Size(max = 500) String comment) {
}
