CREATE TABLE availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id BIGINT,
    venue_id BIGINT,
    available_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    status ENUM('AVAILABLE', 'UNAVAILABLE', 'BOOKED')
        NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_availability_service
        FOREIGN KEY (service_id) REFERENCES services(id),

    CONSTRAINT fk_availability_venue
        FOREIGN KEY (venue_id) REFERENCES venues(id),

    CONSTRAINT chk_availability_resource
        CHECK (
            (service_id IS NOT NULL AND venue_id IS NULL)
            OR
            (service_id IS NULL AND venue_id IS NOT NULL)
        )
);