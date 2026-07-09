CREATE TABLE trips (
    id CHAR(36) PRIMARY KEY,
    origin VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    days INT NOT NULL,
    nights INT NOT NULL,
    people INT NOT NULL,
    budget DECIMAL(14,2) NOT NULL,
    trip_style VARCHAR(32),
    travel_mode VARCHAR(16),
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE hotel_options (
    id CHAR(36) PRIMARY KEY,
    trip_id CHAR(36) NOT NULL,
    external_id VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255),
    address VARCHAR(512),
    price_per_night DECIMAL(14,2),
    currency VARCHAR(8) NOT NULL DEFAULT 'IDR',
    rating DECIMAL(3,1),
    review_count INT,
    image_url VARCHAR(1024),
    source VARCHAR(64),
    source_url VARCHAR(1024),
    score DECIMAL(5,2),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_hotel_options_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE activity_options (
    id CHAR(36) PRIMARY KEY,
    trip_id CHAR(36) NOT NULL,
    external_id VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255),
    address VARCHAR(512),
    price_per_person DECIMAL(14,2),
    currency VARCHAR(8) NOT NULL DEFAULT 'IDR',
    rating DECIMAL(3,1),
    review_count INT,
    image_url VARCHAR(1024),
    source VARCHAR(64),
    source_url VARCHAR(1024),
    duration_hours DECIMAL(4,1),
    score DECIMAL(5,2),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_activity_options_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE route_summaries (
    id CHAR(36) PRIMARY KEY,
    trip_id CHAR(36) NOT NULL,
    origin_label VARCHAR(255),
    origin_lat DOUBLE,
    origin_lng DOUBLE,
    destination_label VARCHAR(255),
    destination_lat DOUBLE,
    destination_lng DOUBLE,
    profile VARCHAR(32),
    distance_km DECIMAL(10,2),
    duration_minutes INT,
    duration_label VARCHAR(64),
    geometry_geojson JSON,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_route_summaries_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE budget_summaries (
    id CHAR(36) PRIMARY KEY,
    trip_id CHAR(36) NOT NULL,
    total_budget DECIMAL(14,2) NOT NULL,
    hotel_total DECIMAL(14,2) NOT NULL DEFAULT 0,
    activity_total DECIMAL(14,2) NOT NULL DEFAULT 0,
    food_total DECIMAL(14,2) NOT NULL DEFAULT 0,
    transport_total DECIMAL(14,2) NOT NULL DEFAULT 0,
    local_transport_total DECIMAL(14,2) NOT NULL DEFAULT 0,
    buffer_total DECIMAL(14,2) NOT NULL DEFAULT 0,
    estimated_total DECIMAL(14,2) NOT NULL DEFAULT 0,
    remaining_budget DECIMAL(14,2) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_budget_summaries_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_hotel_options_trip_id ON hotel_options(trip_id);
CREATE INDEX idx_activity_options_trip_id ON activity_options(trip_id);
CREATE INDEX idx_route_summaries_trip_id ON route_summaries(trip_id);
CREATE INDEX idx_budget_summaries_trip_id ON budget_summaries(trip_id);
