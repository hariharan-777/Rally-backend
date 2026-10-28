CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE venues (
    id             uuid PRIMARY KEY,
    name           varchar(120)           NOT NULL,
    address        varchar(255),
    city           varchar(80)            NOT NULL,
    location       geography(Point, 4326) NOT NULL,
    price_per_hour integer,
    created_at     timestamptz            NOT NULL
);

-- The primary key (venue_id, sport) also serves the "venue offers this sport" filter.
CREATE TABLE venue_sports (
    venue_id uuid        NOT NULL REFERENCES venues (id) ON DELETE CASCADE,
    sport    varchar(32) NOT NULL,
    PRIMARY KEY (venue_id, sport)
);

-- 1-2 character searches: lower(name) LIKE 'sm%'
CREATE INDEX idx_venues_name_prefix ON venues (lower(name) text_pattern_ops);

-- 3+ character searches: lower(name) LIKE '%smash%' and the % similarity operator
CREATE INDEX idx_venues_name_trgm ON venues USING gin (lower(name) gin_trgm_ops);

-- "within X km of this point": ST_DWithin on location
CREATE INDEX idx_venues_location ON venues USING gist (location);
