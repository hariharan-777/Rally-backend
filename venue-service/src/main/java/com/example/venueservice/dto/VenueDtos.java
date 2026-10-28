package com.example.venueservice.dto;

import com.example.venueservice.model.Sport;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public final class VenueDtos {
    private VenueDtos() {
    }

    public record CreateVenueRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 255) String address,
            @NotBlank @Size(max = 80) String city,
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @PositiveOrZero Integer pricePerHour,
            @NotEmpty Set<Sport> sports) {
    }

    /** Query parameters of GET /venues/search. Give q, or lat and lng, or both. */
    public record VenueSearchRequest(
            @Size(max = 50) String q,
            Sport sport,
            @DecimalMin("-90") @DecimalMax("90") Double lat,
            @DecimalMin("-180") @DecimalMax("180") Double lng,
            @DecimalMin("0.1") @DecimalMax("50") Double radiusKm,
            @Min(1) @Max(50) Integer limit) {
    }

    /** distanceMeters is only set when the search was given a location. */
    public record VenueResponse(
            UUID id,
            String name,
            String address,
            String city,
            double latitude,
            double longitude,
            Integer pricePerHour,
            Set<Sport> sports,
            Double distanceMeters) {
    }
}
