CREATE TABLE booking_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_booking_coupons_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_booking_coupons_coupon
        FOREIGN KEY (coupon_id) REFERENCES coupons(id),

    CONSTRAINT uq_booking_coupon
        UNIQUE (booking_id, coupon_id)
);