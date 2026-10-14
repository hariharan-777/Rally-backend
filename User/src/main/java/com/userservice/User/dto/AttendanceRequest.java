package com.userservice.User.dto;

import com.userservice.User.model.AttendanceStatus;
import com.userservice.User.model.Sport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** Body for POST /users/{id}/attendance. sport and gameTime are optional. */
public record AttendanceRequest(
        @NotBlank(message = "required") String gameId,
        @NotNull(message = "required") AttendanceStatus status,
        Sport sport,
        Instant gameTime) {
}
