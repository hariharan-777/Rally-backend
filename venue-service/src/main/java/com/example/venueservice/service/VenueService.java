package com.example.venueservice.service;

import com.example.venueservice.dto.VenueDtos;
import com.example.venueservice.model.Sport;
import com.example.venueservice.model.Venue;
import com.example.venueservice.repository.VenueRepository;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VenueService {
    /** Trigrams are 3 characters, so shorter text cannot use the trigram index. */
    static final int TRIGRAM_MIN_LENGTH = 3;
    static final double DEFAULT_RADIUS_KM = 5;
    static final int DEFAULT_LIMIT = 20;

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    private final VenueRepository repository;

    public VenueService(VenueRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public VenueDtos.VenueResponse create(VenueDtos.CreateVenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.name().trim());
        venue.setAddress(request.address());
        venue.setCity(request.city().trim());
        venue.setLocation(WGS84.createPoint(new Coordinate(request.longitude(), request.latitude())));
        venue.setPricePerHour(request.pricePerHour());
        venue.setSports(EnumSet.copyOf(request.sports()));
        repository.save(venue);
        return new VenueDtos.VenueResponse(venue.getId(), venue.getName(), venue.getAddress(), venue.getCity(),
                request.latitude(), request.longitude(), venue.getPricePerHour(), venue.getSports(), null);
    }

    @Transactional(readOnly = true)
    public VenueDtos.VenueResponse get(UUID id) {
        return repository.findRowById(id).map(this::toResponse).orElseThrow(VenueNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<VenueDtos.VenueResponse> search(VenueDtos.VenueSearchRequest request) {
        if ((request.lat() == null) != (request.lng() == null)) {
            throw new InvalidSearchException("lat and lng must be given together");
        }
        String q = request.q() == null ? "" : request.q().trim().toLowerCase(Locale.ROOT);
        boolean hasLocation = request.lat() != null;
        if (q.isEmpty() && !hasLocation) {
            throw new InvalidSearchException("give a search text q, or lat and lng");
        }

        boolean hasSport = request.sport() != null;
        String sport = hasSport ? request.sport().name() : "";
        double lat = hasLocation ? request.lat() : 0;
        double lng = hasLocation ? request.lng() : 0;
        double radiusMeters = (request.radiusKm() == null ? DEFAULT_RADIUS_KM : request.radiusKm()) * 1000;
        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();

        List<VenueRepository.VenueRow> rows;
        if (q.isEmpty()) {
            rows = repository.findNearby(hasSport, sport, true, lat, lng, radiusMeters, limit);
        } else if (q.length() < TRIGRAM_MIN_LENGTH) {
            rows = repository.searchByPrefix(escapeLike(q) + "%",
                    hasSport, sport, hasLocation, lat, lng, radiusMeters, limit);
        } else {
            rows = repository.searchByName("%" + escapeLike(q) + "%", q,
                    hasSport, sport, hasLocation, lat, lng, radiusMeters, limit);
        }
        return rows.stream().map(this::toResponse).toList();
    }

    /** % and _ are LIKE wildcards; without escaping, a search for "_" would match every venue. */
    static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private VenueDtos.VenueResponse toResponse(VenueRepository.VenueRow row) {
        return new VenueDtos.VenueResponse(row.getId(), row.getName(), row.getAddress(), row.getCity(),
                row.getLatitude(), row.getLongitude(), row.getPricePerHour(), parseSports(row.getSports()),
                row.getDistanceMeters());
    }

    private static Set<Sport> parseSports(String sports) {
        if (sports == null || sports.isEmpty()) {
            return EnumSet.noneOf(Sport.class);
        }
        return Arrays.stream(sports.split(","))
                .map(Sport::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Sport.class)));
    }

    public static final class VenueNotFoundException extends RuntimeException {
    }

    public static final class InvalidSearchException extends RuntimeException {
        public InvalidSearchException(String message) {
            super(message);
        }
    }
}
