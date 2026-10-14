package com.userservice.User.controller;

import com.userservice.User.dto.MatchResultRequest;
import com.userservice.User.dto.MatchResultResponse;
import com.userservice.User.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matches")
public class MatchController {

    private final RatingService ratingService;

    public MatchController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /** Submit a finished match; updates every player's Elo for that sport. */
    @PostMapping("/results")
    public ResponseEntity<MatchResultResponse> recordResult(@Valid @RequestBody MatchResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.recordMatchResult(request));
    }
}
