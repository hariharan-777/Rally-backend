package com.userservice.User.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Whether a player turned up for a game they were booked into.
 * One record per (user, game); recording again for the same game corrects the earlier record.
 */
@Entity
@Table(name = "attendance_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_user_game",
                columnNames = {"user_id", "game_id"}),
        indexes = @Index(name = "idx_attendance_user_recorded", columnList = "user_id, recorded_at"))
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Sport sport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AttendanceStatus status;

    /** When the game was scheduled to start (optional, for display). */
    @Column(name = "game_time")
    private Instant gameTime;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        recordedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public Instant getGameTime() { return gameTime; }
    public void setGameTime(Instant gameTime) { this.gameTime = gameTime; }

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
