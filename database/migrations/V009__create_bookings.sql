CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    venue_id BIGINT,
    booking_date DATE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status ENUM(
        'PENDING',
        'CONFIRMED',
        'CANCELLED',
        'COMPLETED'
    ) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_bookings_event
        FOREIGN KEY (event_id) REFERENCES events(id),

    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_bookings_venue
        FOREIGN KEY (venue_id) REFERENCES venues(id)
);