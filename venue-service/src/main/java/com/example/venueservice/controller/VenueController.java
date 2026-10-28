package com.example.venueservice.controller;

import com.example.venueservice.dto.VenueDtos;
import com.example.venueservice.service.VenueService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/venues")
public class VenueController {
    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    public ResponseEntity<VenueDtos.VenueResponse> create(@Valid @RequestBody VenueDtos.CreateVenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.create(request));
    }

    /** e.g. GET /venues/search?q=smash&sport=BADMINTON&lat=12.9352&lng=77.6245&radiusKm=5 */
    @GetMapping("/search")
    public ResponseEntity<List<VenueDtos.VenueResponse>> search(@Valid @ModelAttribute VenueDtos.VenueSearchRequest request) {
        return ResponseEntity.ok(venueService.search(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueDtos.VenueResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(venueService.get(id));
    }
}
