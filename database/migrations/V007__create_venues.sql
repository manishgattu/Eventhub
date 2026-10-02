CREATE TABLE venues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    capacity INT NOT NULL,
    price DECIMAL(10,2),
    status ENUM('AVAILABLE', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_venues_provider
        FOREIGN KEY (provider_id) REFERENCES service_providers(id)
);