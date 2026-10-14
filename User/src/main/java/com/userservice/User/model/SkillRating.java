package com.userservice.User.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A player's Elo skill rating for one sport. Created lazily (at 1200) the first time
 * the player has a match result recorded in that sport.
 */
@Entity
@Table(name = "skill_ratings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_skill_rating_user_sport",
                columnNames = {"user_id", "sport"}))
public class SkillRating {

    public static final int STARTING_RATING = 1200;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Sport sport;

    @Column(nullable = false)
    private int rating = STARTING_RATING;

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed;

    @Column(nullable = false)
    private int wins;

    @Column(nullable = false)
    private int losses;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Optimistic locking: two match results updating the same rating at once won't silently overwrite each other. */
    @Version
    private Long version;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public SkillRating() {
    }

    public SkillRating(UUID userId, Sport sport) {
        this.userId = userId;
        this.sport = sport;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
