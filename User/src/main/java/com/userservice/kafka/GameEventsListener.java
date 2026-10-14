package com.userservice.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.userservice.User.dto.AttendanceRequest;
import com.userservice.User.dto.MatchResultRequest;
import com.userservice.User.exception.DuplicateResourceException;
import com.userservice.User.model.AttendanceStatus;
import com.userservice.User.model.Sport;
import com.userservice.User.service.RatingService;
import com.userservice.User.service.ReliabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Listens for game events published by other services (e.g. a future booking/game service)
 * and feeds them into the same logic the REST endpoints use.
 *
 * Topic: game-events (override with user.kafka.game-events-topic).
 * Messages are JSON, for example:
 *   {"eventType":"ATTENDANCE_RECORDED","userId":"...","gameId":"g-101","status":"NO_SHOW","sport":"BADMINTON"}
 *   {"eventType":"MATCH_COMPLETED","gameId":"g-101","sport":"BADMINTON","winnerIds":["..."],"loserIds":["..."]}
 *
 * Bad messages are logged and skipped rather than retried forever.
 */
@Component
public class GameEventsListener {

    private static final Logger log = LoggerFactory.getLogger(GameEventsListener.class);

    private final ObjectMapper objectMapper;
    private final RatingService ratingService;
    private final ReliabilityService reliabilityService;

    public GameEventsListener(ObjectMapper objectMapper,
                              RatingService ratingService,
                              ReliabilityService reliabilityService) {
        this.objectMapper = objectMapper;
        this.ratingService = ratingService;
        this.reliabilityService = reliabilityService;
    }

    @KafkaListener(
            topics = "${user.kafka.game-events-topic:game-events}",
            groupId = "${spring.kafka.consumer.group-id:user-service}",
            autoStartup = "${user.kafka.listeners-enabled:true}")
    public void onGameEvent(String payload) {
        GameEvent event;
        try {
            event = objectMapper.readValue(payload, GameEvent.class);
        } catch (RuntimeException e) {
            log.error("Skipping unreadable game event: {}", payload, e);
            return;
        }

        try {
            switch (event.eventType() == null ? "" : event.eventType()) {
                case "ATTENDANCE_RECORDED" -> {
                    reliabilityService.recordAttendance(event.userId(),
                            new AttendanceRequest(event.gameId(), event.status(), event.sport(), event.gameTime()));
                    log.info("Recorded {} for user {} in game {}", event.status(), event.userId(), event.gameId());
                }
                case "MATCH_COMPLETED" -> {
                    ratingService.recordMatchResult(
                            new MatchResultRequest(event.gameId(), event.sport(), event.winnerIds(), event.loserIds()));
                    log.info("Applied Elo updates for game {}", event.gameId());
                }
                default -> log.warn("Ignoring game event with unknown eventType: {}", event.eventType());
            }
        } catch (DuplicateResourceException e) {
            // Kafka can deliver the same message more than once; already-applied results are safe to skip.
            log.info("Skipping duplicate game event: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Failed to process game event: {}", payload, e);
        }
    }

    /** One shape for every game event; fields not used by an event type are simply null. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GameEvent(
            String eventType,
            UUID userId,
            String gameId,
            AttendanceStatus status,
            Sport sport,
            Instant gameTime,
            List<UUID> winnerIds,
            List<UUID> loserIds) {
    }
}
