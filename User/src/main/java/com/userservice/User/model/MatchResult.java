package com.userservice.User.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Remembers which games have already been applied to Elo ratings, so the same result
 * (e.g. a retried request or a re-delivered Kafka message) is never counted twice.
 */
@Entity
@Table(name = "match_results",
        uniqueConstraints = @UniqueConstraint(name = "uk_match_result_game", columnNames = "game_id"))
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Sport sport;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @PrePersist
    void onCreate() {
        if (recordedAt == null) {
            recordedAt = Instant.now();
        }
    }

    public MatchResult() {
    }

    public MatchResult(String gameId, Sport sport) {
        this.gameId = gameId;
        this.sport = sport;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
