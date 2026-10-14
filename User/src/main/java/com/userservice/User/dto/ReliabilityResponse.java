package com.userservice.User.dto;

import com.userservice.User.model.AttendanceRecord;
import com.userservice.User.model.AttendanceStatus;
import com.userservice.User.model.Sport;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * score: 0-100, calculated over the last 20 games (see ReliabilityCalculator).
 * tier:  NEW / RELIABLE / FAIR / UNRELIABLE.
 * attended / lateCancellations / noShows: lifetime totals.
 */
public record ReliabilityResponse(
        UUID userId,
        int score,
        String tier,
        int gamesConsidered,
        long attended,
        long lateCancellations,
        long noShows,
        List<AttendanceEntry> recent) {

    public record AttendanceEntry(String gameId, Sport sport, AttendanceStatus status,
                                  Instant gameTime, Instant recordedAt) {

        public static AttendanceEntry from(AttendanceRecord r) {
            return new AttendanceEntry(r.getGameId(), r.getSport(), r.getStatus(), r.getGameTime(), r.getRecordedAt());
        }
    }
}
