CREATE TABLE service_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    business_name VARCHAR(200) NOT NULL,
    description TEXT,
    phone VARCHAR(20),
    email VARCHAR(255),
    website VARCHAR(255),
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED')
        NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_service_providers_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT uq_service_provider_user
        UNIQUE (user_id)
);