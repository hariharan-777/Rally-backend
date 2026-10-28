package com.example.venueservice.repository;

import com.example.venueservice.model.Venue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The search queries are native because they use PostGIS (ST_DWithin, ST_Distance) and
 * pg_trgm (the % similarity operator), which JPQL has no syntax for.
 *
 * Optional filters are switched off with boolean flags (hasSport, hasLocation) rather than
 * null parameters: Postgres cannot infer the type of a null bind value in these expressions.
 */
public interface VenueRepository extends JpaRepository<Venue, UUID> {

    String COLUMNS = """
            SELECT v.id AS "id",
                   v.name AS "name",
                   v.address AS "address",
                   v.city AS "city",
                   ST_Y(CAST(v.location AS geometry)) AS "latitude",
                   ST_X(CAST(v.location AS geometry)) AS "longitude",
                   v.price_per_hour AS "pricePerHour",
                   (SELECT string_agg(s.sport, ',') FROM venue_sports s WHERE s.venue_id = v.id) AS "sports"
            """;

    String DISTANCE_FROM_VENUES = """
            , CASE WHEN :hasLocation
                   THEN ST_Distance(v.location, CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography))
              END AS "distanceMeters"
            FROM venues v
            """;

    String FILTERS = """
              AND (:hasSport = false
                   OR EXISTS (SELECT 1 FROM venue_sports s WHERE s.venue_id = v.id AND s.sport = :sport))
              AND (:hasLocation = false
                   OR ST_DWithin(v.location, CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography), :radiusMeters))
            """;

    @Query(value = COLUMNS + """
            , CAST(NULL AS double precision) AS "distanceMeters"
            FROM venues v
            WHERE v.id = :id
            """, nativeQuery = true)
    Optional<VenueRow> findRowById(@Param("id") UUID id);

    /** 1-2 character searches: names starting with the text. Uses idx_venues_name_prefix. */
    @Query(value = COLUMNS + DISTANCE_FROM_VENUES + """
            WHERE lower(v.name) LIKE :pattern ESCAPE '\\'
            """ + FILTERS + """
            ORDER BY "distanceMeters" NULLS LAST, lower(v.name)
            LIMIT :limit
            """, nativeQuery = true)
    List<VenueRow> searchByPrefix(@Param("pattern") String pattern,
                                  @Param("hasSport") boolean hasSport,
                                  @Param("sport") String sport,
                                  @Param("hasLocation") boolean hasLocation,
                                  @Param("lat") double lat,
                                  @Param("lng") double lng,
                                  @Param("radiusMeters") double radiusMeters,
                                  @Param("limit") int limit);

    /** 3+ character searches: names containing the text, or close to it (typos). Uses idx_venues_name_trgm. */
    @Query(value = COLUMNS + DISTANCE_FROM_VENUES + """
            WHERE (lower(v.name) LIKE :pattern ESCAPE '\\' OR lower(v.name) % :q)
            """ + FILTERS + """
            ORDER BY similarity(lower(v.name), :q) DESC, "distanceMeters" NULLS LAST
            LIMIT :limit
            """, nativeQuery = true)
    List<VenueRow> searchByName(@Param("pattern") String pattern,
                                @Param("q") String q,
                                @Param("hasSport") boolean hasSport,
                                @Param("sport") String sport,
                                @Param("hasLocation") boolean hasLocation,
                                @Param("lat") double lat,
                                @Param("lng") double lng,
                                @Param("radiusMeters") double radiusMeters,
                                @Param("limit") int limit);

    /** No search text: the nearest venues to a point. Uses idx_venues_location. */
    @Query(value = COLUMNS + DISTANCE_FROM_VENUES + """
            WHERE true
            """ + FILTERS + """
            ORDER BY "distanceMeters"
            LIMIT :limit
            """, nativeQuery = true)
    List<VenueRow> findNearby(@Param("hasSport") boolean hasSport,
                              @Param("sport") String sport,
                              @Param("hasLocation") boolean hasLocation,
                              @Param("lat") double lat,
                              @Param("lng") double lng,
                              @Param("radiusMeters") double radiusMeters,
                              @Param("limit") int limit);

    interface VenueRow {
        UUID getId();
        String getName();
        String getAddress();
        String getCity();
        Double getLatitude();
        Double getLongitude();
        Integer getPricePerHour();
        /** Comma-separated Sport names, e.g. "BADMINTON,SQUASH". */
        String getSports();
        Double getDistanceMeters();
    }
}
