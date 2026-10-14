package com.userservice.User.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * One player's rating of another player after a game they played together.
 * A rater can rate the same player only once per game (enforced by the unique constraint).
 */
@Entity
@Table(name = "peer_ratings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_peer_rating_rater_ratee_game",
                columnNames = {"rater_id", "ratee_id", "game_id"}),
        indexes = @Index(name = "idx_peer_rating_ratee", columnList = "ratee_id"))
public class PeerRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rater_id", nullable = false)
    private UUID raterId;

    @Column(name = "ratee_id", nullable = false)
    private UUID rateeId;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Sport sport;

    /** 1-5: how good a player they are. */
    @Column(name = "skill_score", nullable = false)
    private int skillScore;

    /** 1-5: fair play, attitude, punctuality during the game. */
    @Column(name = "sportsmanship_score", nullable = false)
    private int sportsmanshipScore;

    @Column(length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRaterId() { return raterId; }
    public void setRaterId(UUID raterId) { this.raterId = raterId; }

    public UUID getRateeId() { return rateeId; }
    public void setRateeId(UUID rateeId) { this.rateeId = rateeId; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public int getSkillScore() { return skillScore; }
    public void setSkillScore(int skillScore) { this.skillScore = skillScore; }

    public int getSportsmanshipScore() { return sportsmanshipScore; }
    public void setSportsmanshipScore(int sportsmanshipScore) { this.sportsmanshipScore = sportsmanshipScore; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
