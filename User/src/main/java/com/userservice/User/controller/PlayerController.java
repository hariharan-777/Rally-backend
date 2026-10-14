package com.userservice.User.controller;

import com.userservice.User.dto.AttendanceRequest;
import com.userservice.User.dto.PeerRatingRequest;
import com.userservice.User.dto.PeerRatingResponse;
import com.userservice.User.dto.PlayerProfileResponse;
import com.userservice.User.dto.ReliabilityResponse;
import com.userservice.User.service.RatingService;
import com.userservice.User.service.ReliabilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Player-facing endpoints: ratings, attendance and the combined player profile. */
@RestController
@RequestMapping("/users/{id}")
public class PlayerController {

    private final RatingService ratingService;
    private final ReliabilityService reliabilityService;

    public PlayerController(RatingService ratingService, ReliabilityService reliabilityService) {
        this.ratingService = ratingService;
        this.reliabilityService = reliabilityService;
    }

    /** Rate the player {id} after a game. */
    @PostMapping("/ratings")
    public ResponseEntity<PeerRatingResponse> ratePlayer(@PathVariable UUID id,
                                                         @Valid @RequestBody PeerRatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.ratePlayer(id, request));
    }

    /** All ratings player {id} has received, newest first. */
    @GetMapping("/ratings")
    public ResponseEntity<List<PeerRatingResponse>> getRatings(@PathVariable UUID id) {
        return ResponseEntity.ok(ratingService.getRatingsReceived(id));
    }

    /** Record whether player {id} turned up for a game. Returns their updated reliability. */
    @PostMapping("/attendance")
    public ResponseEntity<ReliabilityResponse> recordAttendance(@PathVariable UUID id,
                                                                @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(reliabilityService.recordAttendance(id, request));
    }

    /** Reliability score plus no-show history. */
    @GetMapping("/reliability")
    public ResponseEntity<ReliabilityResponse> getReliability(@PathVariable UUID id) {
        return ResponseEntity.ok(reliabilityService.getReliability(id));
    }

    /** Elo + peer ratings per sport, plus reliability, in one call. */
    @GetMapping("/profile")
    public ResponseEntity<PlayerProfileResponse> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ratingService.getPlayerProfile(id));
    }
}
