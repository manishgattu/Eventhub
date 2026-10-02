CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_type_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    event_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    guest_count INT DEFAULT 0,
    status ENUM('DRAFT', 'PLANNED', 'CONFIRMED', 'COMPLETED', 'CANCELLED')
        NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_events_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_events_event_type
        FOREIGN KEY (event_type_id) REFERENCES event_types(id)
);